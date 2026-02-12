package rabbitmq.consumer.example.messaging;

import java.util.List;

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

    @Override
    public String toString() {
        return "UploadEvent{videoId=" + videoId + ", title='" + title + "', author='" + authorUsername +
                "', sizeMb=" + videoSizeMb + ", tags=" + tags + "}";
    }
}
