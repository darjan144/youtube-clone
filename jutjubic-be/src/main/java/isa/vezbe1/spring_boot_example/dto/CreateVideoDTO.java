package isa.vezbe1.spring_boot_example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateVideoDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Thumbnail path is required")
    private String thumbnailPath;

    @NotBlank(message = "Video path is required")
    private String videoPath;

    @NotNull(message = "Video size is required")
    private Double videoSizeMb;

    private List<String> tags;

    public CreateVideoDTO() {
    }

    public CreateVideoDTO(String title, String description, String thumbnailPath,
                          String videoPath, Double videoSizeMb, List<String> tags) {
        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.videoPath = videoPath;
        this.videoSizeMb = videoSizeMb;
        this.tags = tags;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}