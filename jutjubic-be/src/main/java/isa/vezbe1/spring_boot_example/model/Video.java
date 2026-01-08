package isa.vezbe1.spring_boot_example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_path", nullable = false)
    private String thumbnailPath;

    @Column(name = "video_path", nullable = false)
    private String videoPath;

    @Column(name = "video_size_mb")
    private Double videoSizeMb;

    @Column(name = "view_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "video_tag_mapping",
            joinColumns = @JoinColumn(name = "video_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<VideoTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Comment> comments = new HashSet<>();

    public Video() {
    }

    public Video(String title, String description, String thumbnailPath, String videoPath,
                 User uploader, Timestamp createdAt) {
        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.videoPath = videoPath;
        this.uploader = uploader;
        this.createdAt = createdAt;
        this.viewCount = 0L;
    }

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

    public void incrementViewCount() {
        this.viewCount++;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public Set<VideoTag> getTags() {
        return tags;
    }

    public void setTags(Set<VideoTag> tags) {
        this.tags = tags;
    }

    public void addTag(VideoTag tag) {
        this.tags.add(tag);
        tag.getVideos().add(this);
    }

    public void removeTag(VideoTag tag) {
        this.tags.remove(tag);
        tag.getVideos().remove(this);
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setVideo(this);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setVideo(null);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = new Timestamp(System.currentTimeMillis());
        if (viewCount == null) {
            viewCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

}
