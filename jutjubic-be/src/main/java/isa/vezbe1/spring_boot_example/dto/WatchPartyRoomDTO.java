package isa.vezbe1.spring_boot_example.dto;

import java.util.List;

public class WatchPartyRoomDTO {

    private String roomId;
    private UserDTO owner;
    private List<UserDTO> members;

    public WatchPartyRoomDTO() {
    }

    public WatchPartyRoomDTO(String roomId, UserDTO owner, List<UserDTO> members) {
        this.roomId = roomId;
        this.owner = owner;
        this.members = members;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public UserDTO getOwner() {
        return owner;
    }

    public void setOwner(UserDTO owner) {
        this.owner = owner;
    }

    public List<UserDTO> getMembers() {
        return members;
    }

    public void setMembers(List<UserDTO> members) {
        this.members = members;
    }
}
