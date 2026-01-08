package isa.vezbe1.spring_boot_example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCommentDTO {

    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String text;

    @NotNull(message = "Video ID is required")
    private Long videoId;

    public CreateCommentDTO() {
    }

    public CreateCommentDTO(String text, Long videoId) {
        this.text = text;
        this.videoId = videoId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}