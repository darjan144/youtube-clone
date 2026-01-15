package isa.vezbe1.spring_boot_example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

//multipart file
public class VideoUploadDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private List<String> tags;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    public VideoUploadDTO() {
    }

    public VideoUploadDTO(String title, String description, List<String> tags, String location) {
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.location = location;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}