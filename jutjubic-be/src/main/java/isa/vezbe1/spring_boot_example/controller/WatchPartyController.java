package isa.vezbe1.spring_boot_example.controller;

import isa.vezbe1.spring_boot_example.dto.WatchPartyPlayDTO;
import isa.vezbe1.spring_boot_example.dto.WatchPartyRoomDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.WatchPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/watchparty")
@CrossOrigin(origins = "http://localhost:5173")
public class WatchPartyController {

    @Autowired
    private WatchPartyService watchPartyService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createRoom() {
        try {
            User currentUser = authenticationService.getCurrentUser();
            WatchPartyRoomDTO room = watchPartyService.createRoom(currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/join/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            WatchPartyRoomDTO room = watchPartyService.joinRoom(roomId, currentUser);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        try {
            WatchPartyRoomDTO room = watchPartyService.getRoom(roomId);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/leave/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            watchPartyService.leaveRoom(roomId, currentUser);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Left room successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @MessageMapping("/watchparty/play")
    public void playVideo(WatchPartyPlayDTO playDTO) {
        String roomId = playDTO.getRoomId();
        if (roomId != null && watchPartyService.roomExists(roomId)) {
            messagingTemplate.convertAndSend("/topic/watchparty/" + roomId, playDTO);
        }
    }

    private ResponseEntity<Map<String, String>> errorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}
