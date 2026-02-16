package isa.vezbe1.spring_boot_example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import isa.vezbe1.spring_boot_example.model.Comment;

import java.sql.Timestamp;

@Schema(description = "Comment information")
public class CommentDTO {

    @Schema(description = "Comment ID", example = "1")
    private Long id;

    @Schema(description = "Comment text", example = "Great video!")
    private String text;

    @Schema(description = "Comment creation timestamp")
    private Timestamp createdAt;

    @Schema(description = "Comment author")
    private UserDTO author;

    @Schema(description = "ID of the video this comment belongs to", example = "5")
    private Long videoId;

    public CommentDTO() {
    }

    public CommentDTO(Long id, String text, Timestamp createdAt, UserDTO author, Long videoId) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.author = author;
        this.videoId = videoId;
    }

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        this.author = new UserDTO(comment.getAuthor());
        this.videoId = comment.getVideo().getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}