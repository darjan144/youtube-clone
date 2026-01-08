package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.CommentDTO;
import isa.vezbe1.spring_boot_example.dto.CreateCommentDTO;
import isa.vezbe1.spring_boot_example.model.Comment;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.repository.CommentRepository;
import isa.vezbe1.spring_boot_example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final int MAX_COMMENTS_PER_HOUR = 60;

    public List<CommentDTO> getCommentsByVideoId(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        List<Comment> comments = commentRepository.findByVideoOrderByCreatedAtDesc(video);
        return comments.stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }

    public Page<CommentDTO> getCommentsByVideoId(Long videoId, Pageable pageable) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Page<Comment> comments = commentRepository.findByVideoOrderByCreatedAtDesc(video, pageable);
        return comments.map(CommentDTO::new);
    }


    @Transactional
    public CommentDTO createComment(CreateCommentDTO createCommentDTO, User author) {

        Timestamp oneHourAgo = new Timestamp(System.currentTimeMillis() - 3600000); // 1 hour in milliseconds
        Long commentCount = commentRepository.countByAuthorSince(author, oneHourAgo);

        if (commentCount >= MAX_COMMENTS_PER_HOUR) {
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

        return new CommentDTO(savedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if user is the author or admin
        if (!comment.getAuthor().getId().equals(user.getId()) &&
                !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }


    public List<CommentDTO> getCommentsByAuthor(User author) {
        List<Comment> comments = commentRepository.findByAuthorOrderByCreatedAtDesc(author);
        return comments.stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }


    public Long getCommentCount(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        return commentRepository.countByVideo(video);
    }
}