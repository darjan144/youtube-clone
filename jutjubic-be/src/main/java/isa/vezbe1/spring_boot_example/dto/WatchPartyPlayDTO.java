package isa.vezbe1.spring_boot_example.dto;

public class WatchPartyPlayDTO {

    private String roomId;
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
