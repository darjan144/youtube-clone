package isa.vezbe1.spring_boot_example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Watch Party", description = "Watch party room creation, joining, and synchronized playback")
public class WatchPartyController {

    @Autowired
    private WatchPartyService watchPartyService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "Create a watch party room", description = "Creates a new watch party room. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Room created"),
            @ApiResponse(responseCode = "400", description = "Failed to create room"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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

    @Operation(summary = "Join a watch party room", description = "Joins an existing watch party room. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Joined room"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @PostMapping("/join/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> joinRoom(@Parameter(description = "Room ID") @PathVariable String roomId) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            WatchPartyRoomDTO room = watchPartyService.joinRoom(roomId, currentUser);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Get watch party room", description = "Returns details of a watch party room. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room details returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @GetMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRoom(@Parameter(description = "Room ID") @PathVariable String roomId) {
        try {
            WatchPartyRoomDTO room = watchPartyService.getRoom(roomId);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return errorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Leave a watch party room", description = "Leaves a watch party room. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Left room"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @PostMapping("/leave/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> leaveRoom(@Parameter(description = "Room ID") @PathVariable String roomId) {
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
