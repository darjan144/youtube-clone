package isa.vezbe1.spring_boot_example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import isa.vezbe1.spring_boot_example.model.Video;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "Video information")
public class VideoDTO {

    @Schema(description = "Video ID", example = "1")
    private Long id;

    @Schema(description = "Video title", example = "My First Video")
    private String title;

    @Schema(description = "Video description", example = "A short description of the video")
    private String description;

    @Schema(description = "Path to thumbnail image", example = "/uploads/thumbnails/thumb1.jpg")
    private String thumbnailPath;

    @Schema(description = "Path to video file", example = "/uploads/videos/video1.mp4")
    private String videoPath;

    @Schema(description = "Video file size in MB", example = "25.5")
    private Double videoSizeMb;

    @Schema(description = "Number of views", example = "1500")
    private Long viewCount;

    @Schema(description = "Video upload timestamp")
    private Timestamp createdAt;

    @Schema(description = "User who uploaded the video")
    private UserDTO uploader;

    @Schema(description = "Video tags", example = "[\"music\", \"tutorial\"]")
    private List<String> tags;

    @Schema(description = "Number of comments", example = "42")
    private Long commentCount;

    public VideoDTO() {
    }

    public VideoDTO(Long id, String title, String description, String thumbnailPath,
                    String videoPath, Double videoSizeMb, Long viewCount, Timestamp createdAt,
                    UserDTO uploader, List<String> tags, Long commentCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.videoPath = videoPath;
        this.videoSizeMb = videoSizeMb;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.uploader = uploader;
        this.tags = tags;
        this.commentCount = commentCount;
    }

    public VideoDTO(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.description = video.getDescription();
        this.thumbnailPath = video.getThumbnailPath();
        this.videoPath = video.getVideoPath();
        this.videoSizeMb = video.getVideoSizeMb();
        this.viewCount = video.getViewCount();
        this.createdAt = video.getCreatedAt();
        this.uploader = new UserDTO(video.getUploader());
        this.tags = video.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList());
        this.commentCount = (long) video.getComments().size();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public Double getVideoSizeMb() {
        return videoSizeMb;
    }

    public void setVideoSizeMb(Double videoSizeMb) {
        this.videoSizeMb = videoSizeMb;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public UserDTO getUploader() {
        return uploader;
    }

    public void setUploader(UserDTO uploader) {
        this.uploader = uploader;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
}