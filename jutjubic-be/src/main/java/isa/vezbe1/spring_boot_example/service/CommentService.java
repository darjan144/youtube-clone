package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.CommentDTO;
import isa.vezbe1.spring_boot_example.dto.CreateCommentDTO;
import isa.vezbe1.spring_boot_example.model.Comment;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.repository.CommentRepository;
import isa.vezbe1.spring_boot_example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;  // YOUR existing RedisTemplate

    private static final int MAX_COMMENTS_PER_HOUR = 60;
    private static final String COMMENT_RATE_LIMIT_PREFIX = "comment_rate_limit:";

    // Cacheable - comments will be cached
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#videoId")
    public List<CommentDTO> getCommentsByVideoId(Long videoId) {
        System.out.println("Fetching comments from database for video: " + videoId);
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        List<Comment> comments = commentRepository.findByVideoOrderByCreatedAtDesc(video);
        return comments.stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }

    // Cacheable - paginated comments will be cached
    @Transactional(readOnly = true)
    @Cacheable(value = "commentPages", key = "#videoId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CommentDTO> getCommentsByVideoId(Long videoId, Pageable pageable) {
        System.out.println("Fetching paginated comments from database for video: " + videoId + ", page: " + pageable.getPageNumber());
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Page<Comment> comments = commentRepository.findByVideoOrderByCreatedAtDesc(video, pageable);
        return comments.map(CommentDTO::new);
    }

    // CacheEvict - when a comment is created, invalidate the cache for that video
    @Transactional
    @CacheEvict(value = {"comments", "commentPages"}, allEntries = true)
    public CommentDTO createComment(CreateCommentDTO createCommentDTO, User author) {

        // Redis-based rate limiting
        String rateLimitKey = COMMENT_RATE_LIMIT_PREFIX + author.getId();

        // Get current count from Redis
        Integer currentCount = redisTemplate.opsForValue().get(rateLimitKey);

        if (currentCount != null && currentCount >= MAX_COMMENTS_PER_HOUR) {
            throw new RuntimeException("Rate limit exceeded. Maximum " + MAX_COMMENTS_PER_HOUR + " comments per hour allowed.");
        }

        Video video = videoRepository.findById(createCommentDTO.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Comment comment = new Comment();
        comment.setText(createCommentDTO.getText());
        comment.setVideo(video);
        comment.setAuthor(author);
        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Comment savedComment = commentRepository.save(comment);

        // Increment the rate limit counter in Redis
        if (currentCount == null) {
            // First comment in this hour - set counter to 1 with 1 hour expiration
            redisTemplate.opsForValue().set(rateLimitKey, 1, 1, TimeUnit.HOURS);
        } else {
            // Increment the counter
            redisTemplate.opsForValue().increment(rateLimitKey);
        }

        return new CommentDTO(savedComment);
    }

    // CacheEvict - when a comment is deleted, invalidate the cache
    @Transactional
    @CacheEvict(value = {"comments", "commentPages"}, allEntries = true)
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId()) &&
                !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByAuthor(User author) {
        List<Comment> comments = commentRepository.findByAuthorOrderByCreatedAtDesc(author);
        return comments.stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getCommentCount(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        return commentRepository.countByVideo(video);
    }

    // Helper method to check remaining comments for a user (useful for debugging)
    public int getRemainingComments(User user) {
        String rateLimitKey = COMMENT_RATE_LIMIT_PREFIX + user.getId();
        Integer currentCount = redisTemplate.opsForValue().get(rateLimitKey);

        if (currentCount == null) {
            return MAX_COMMENTS_PER_HOUR;
        }

        return Math.max(0, MAX_COMMENTS_PER_HOUR - currentCount);
    }
}