package isa.vezbe1.spring_boot_example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for creating a comment")
public class CreateCommentDTO {

    @Schema(description = "Comment text (1-2000 characters)", example = "Great video, thanks for sharing!")
    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String text;

    @Schema(description = "ID of the video to comment on", example = "1")
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