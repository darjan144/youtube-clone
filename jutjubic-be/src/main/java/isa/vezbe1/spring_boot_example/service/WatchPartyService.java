package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.UserDTO;
import isa.vezbe1.spring_boot_example.dto.WatchPartyRoomDTO;
import isa.vezbe1.spring_boot_example.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WatchPartyService {

    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

    public WatchPartyRoomDTO createRoom(User owner) {
        String roomId = UUID.randomUUID().toString();
        Room room = new Room(roomId, owner);
        room.members.put(owner.getId(), owner);
        rooms.put(roomId, room);
        return toDTO(room);
    }

    public WatchPartyRoomDTO joinRoom(String roomId, User user) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }
        room.members.put(user.getId(), user);
        return toDTO(room);
    }

    public WatchPartyRoomDTO getRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }
        return toDTO(room);
    }

    public void leaveRoom(String roomId, User user) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }
        if (room.owner.getId().equals(user.getId())) {
            rooms.remove(roomId);
        } else {
            room.members.remove(user.getId());
        }
    }

    public boolean isOwner(String roomId, User user) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return false;
        }
        return room.owner.getId().equals(user.getId());
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    private WatchPartyRoomDTO toDTO(Room room) {
        List<UserDTO> memberDTOs = new ArrayList<>();
        for (User member : room.members.values()) {
            memberDTOs.add(new UserDTO(member));
        }
        return new WatchPartyRoomDTO(room.roomId, new UserDTO(room.owner), memberDTOs);
    }

    private static class Room {
        final String roomId;
        final User owner;
        final ConcurrentHashMap<Long, User> members = new ConcurrentHashMap<>();

        Room(String roomId, User owner) {
            this.roomId = roomId;
            this.owner = owner;
        }
    }
}
