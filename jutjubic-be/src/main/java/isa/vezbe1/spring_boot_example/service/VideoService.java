package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.CreateVideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoUploadDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.model.VideoTag;
import isa.vezbe1.spring_boot_example.repository.VideoRepository;
import isa.vezbe1.spring_boot_example.repository.VideoTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoTagRepository videoTagRepository;

    @Autowired
    @Qualifier("thumbnailRedisTemplate")
    private RedisTemplate<String, byte[]> redisTemplate;

    @Transactional(readOnly = true)
    public List<VideoDTO> getAllVideos() {
        List<Video> videos = videoRepository.findAllByOrderByCreatedAtDesc();
        return videos.stream()
                .map(VideoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VideoDTO> getAllVideos(Pageable pageable) {
        Page<Video> videos = videoRepository.findAllByOrderByCreatedAtDesc(pageable);
        return videos.map(VideoDTO::new);
    }

    @Transactional(readOnly = true)
    public VideoDTO getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
        return new VideoDTO(video);
    }


    @Transactional(rollbackFor = Exception.class, timeout = 300)
    public VideoDTO uploadVideo(
            MultipartFile videoFile,
            MultipartFile thumbnailFile,
            VideoUploadDTO uploadDTO,
            User uploader) throws IOException {

        // Step 1: Validate files
        validateVideoFile(videoFile);
        validateThumbnailFile(thumbnailFile);

        // Step 2: Create video entity
        Video video = new Video();
        video.setTitle(uploadDTO.getTitle());
        video.setDescription(uploadDTO.getDescription());
        video.setLocation(uploadDTO.getLocation());
        video.setUploader(uploader);
        video.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        video.setVideoSizeMb(videoFile.getSize() / (1024.0 * 1024.0));

        String videoPath = null;
        String thumbnailPath = null;

        try {
            // Step 3: Save files to disk
            videoPath = saveVideoFile(videoFile);
            thumbnailPath = saveThumbnailFile(thumbnailFile);

            video.setVideoPath(videoPath);
            video.setThumbnailPath(thumbnailPath);

            // Step 4: Handle tags (transactional)
            Set<VideoTag> tags = new HashSet<>();
            if (uploadDTO.getTags() != null && !uploadDTO.getTags().isEmpty()) {
                for (String tagName : uploadDTO.getTags()) {
                    VideoTag tag = videoTagRepository.findByNameIgnoreCase(tagName)
                            .orElseGet(() -> {
                                VideoTag newTag = new VideoTag(tagName);
                                return videoTagRepository.save(newTag);
                            });
                    tags.add(tag);
                }
            }
            video.setTags(tags);

            // Step 5: Save video to database (transactional)
            video = videoRepository.save(video);

            // Step 6: Cache thumbnail in Redis
            cacheThumbnail(video.getId(), thumbnailPath);

            return new VideoDTO(video);

        } catch (Exception e) {
            // Rollback: Delete uploaded files if DB operation fails
            cleanupFiles(videoPath, thumbnailPath);
            throw new RuntimeException("Failed to upload video: " + e.getMessage(), e);
        }
    }


    @Transactional
    public VideoDTO createVideo(CreateVideoDTO createVideoDTO, User uploader) {

        if (createVideoDTO.getVideoSizeMb() > 200) {
            throw new RuntimeException("Video size cannot exceed 200MB");
        }

        Video video = new Video();
        video.setTitle(createVideoDTO.getTitle());
        video.setDescription(createVideoDTO.getDescription());
        video.setThumbnailPath(createVideoDTO.getThumbnailPath());
        video.setVideoPath(createVideoDTO.getVideoPath());
        video.setVideoSizeMb(createVideoDTO.getVideoSizeMb());
        video.setUploader(uploader);
        video.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Set<VideoTag> tags = new HashSet<>();
        if (createVideoDTO.getTags() != null && !createVideoDTO.getTags().isEmpty()) {
            for (String tagName : createVideoDTO.getTags()) {
                VideoTag tag = videoTagRepository.findByNameIgnoreCase(tagName)
                        .orElseGet(() -> {
                            VideoTag newTag = new VideoTag(tagName);
                            return videoTagRepository.save(newTag);
                        });
                tags.add(tag);
            }
        }
        video.setTags(tags);

        Video savedVideo = videoRepository.save(video);

        return new VideoDTO(savedVideo);
    }

    @Transactional
    public void incrementViewCount(Long videoId) {
        videoRepository.incrementViewCount(videoId);
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> getVideosByUploader(User uploader) {
        List<Video> videos = videoRepository.findByUploaderOrderByCreatedAtDesc(uploader);
        return videos.stream()
                .map(VideoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> searchVideosByTitle(String title) {
        List<Video> videos = videoRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
        return videos.stream()
                .map(VideoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVideo(Long videoId, User user) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (!video.getUploader().getId().equals(user.getId()) &&
                !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("You don't have permission to delete this video");
        }

        videoRepository.delete(video);
    }

    public byte[] getCachedThumbnail(Long videoId) {
        String cacheKey = "thumbnail:" + videoId;
        return redisTemplate.opsForValue().get(cacheKey);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file is required");
        }

        // Check file size (max 200MB)
        long maxSize = 200L * 1024 * 1024; // 200MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Video file exceeds maximum size of 200MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("video/mp4")) {
            throw new IllegalArgumentException("Only MP4 format is supported");
        }
    }

    private void validateThumbnailFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Thumbnail is required");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/jpeg") &&
                        !contentType.startsWith("image/png"))) {
            throw new IllegalArgumentException("Thumbnail must be JPEG or PNG");
        }
    }

    private String saveVideoFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path videoDir = Paths.get(uploadDir, "videos");
        Files.createDirectories(videoDir);

        Path filePath = videoDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/videos/" + filename;
    }

    private String saveThumbnailFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path thumbDir = Paths.get(uploadDir, "thumbnails");
        Files.createDirectories(thumbDir);

        Path filePath = thumbDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/thumbnails/" + filename;
    }

    private void cacheThumbnail(Long videoId, String thumbnailPath) throws IOException {
        // Read thumbnail file and cache in Redis
        Path path = Paths.get(uploadDir, thumbnailPath.replace("/uploads/", ""));
        if (Files.exists(path)) {
            byte[] thumbnailData = Files.readAllBytes(path);

            String cacheKey = "thumbnail:" + videoId;
            redisTemplate.opsForValue().set(cacheKey, thumbnailData, 24, TimeUnit.HOURS);
        }
    }

    private void cleanupFiles(String videoPath, String thumbnailPath) {
        try {
            if (videoPath != null) {
                Path vPath = Paths.get(uploadDir, videoPath.replace("/uploads/", ""));
                Files.deleteIfExists(vPath);
            }
            if (thumbnailPath != null) {
                Path tPath = Paths.get(uploadDir, thumbnailPath.replace("/uploads/", ""));
                Files.deleteIfExists(tPath);
            }
        } catch (IOException e) {
            // Log but don't throw - we're already in error handling
            System.err.println("Failed to cleanup files: " + e.getMessage());
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed";
        }
        // Remove potentially dangerous characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}