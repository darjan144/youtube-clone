package isa.vezbe1.spring_boot_example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Watch party video playback command")
public class WatchPartyPlayDTO {

    @Schema(description = "Room ID", example = "abc12345")
    private String roomId;

    @Schema(description = "Video ID to play", example = "1")
    private Long videoId;

    public WatchPartyPlayDTO() {
    }

    public WatchPartyPlayDTO(String roomId, Long videoId) {
        this.roomId = roomId;
        this.videoId = videoId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}
