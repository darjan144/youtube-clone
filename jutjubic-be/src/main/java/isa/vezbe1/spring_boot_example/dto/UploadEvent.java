package isa.vezbe1.spring_boot_example.dto;

import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.model.VideoTag;

import java.util.List;
import java.util.stream.Collectors;

public class UploadEvent {

    private Long videoId;
    private String title;
    private String description;
    private Double videoSizeMb;
    private String authorUsername;
    private long createdAt;
    private List<String> tags;

    public UploadEvent() {
    }

    public UploadEvent(Long videoId, String title, String description, Double videoSizeMb,
                       String authorUsername, long createdAt, List<String> tags) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.videoSizeMb = videoSizeMb;
        this.authorUsername = authorUsername;
        this.createdAt = createdAt;
        this.tags = tags;
    }

    public static UploadEvent fromVideo(Video video) {
        UploadEvent event = new UploadEvent();
        event.setVideoId(video.getId());
        event.setTitle(video.getTitle());
        event.setDescription(video.getDescription() != null ? video.getDescription() : "");
        event.setVideoSizeMb(video.getVideoSizeMb() != null ? video.getVideoSizeMb() : 0.0);
        event.setAuthorUsername(video.getUploader().getUsername());
        event.setCreatedAt(video.getCreatedAt().getTime());
        event.setTags(video.getTags().stream()
                .map(VideoTag::getName)
                .collect(Collectors.toList()));
        return event;
    }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getVideoSizeMb() { return videoSizeMb; }
    public void setVideoSizeMb(Double videoSizeMb) { this.videoSizeMb = videoSizeMb; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
