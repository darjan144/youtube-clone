package isa.vezbe1.spring_boot_example.controller;

import isa.vezbe1.spring_boot_example.dto.CreateVideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "http://localhost:5173")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<?> getAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            if (page < 0 || size <= 0 || size > 100) {
                Pageable pageable = PageRequest.of(0, 20);
                Page<VideoDTO> videos = videoService.getAllVideos(pageable);
                return ResponseEntity.ok(videos);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<VideoDTO> videos = videoService.getAllVideos(pageable);

            return ResponseEntity.ok(videos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch videos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getVideoById(@PathVariable Long id) {
        try {
            VideoDTO video = videoService.getVideoById(id);
            return ResponseEntity.ok(video);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createVideo(@Valid @RequestBody CreateVideoDTO createVideoDTO) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            VideoDTO video = videoService.createVideo(createVideoDTO, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(video);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementViewCount(@PathVariable Long id) {
        try {
            videoService.incrementViewCount(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "View count incremented");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchVideos(@RequestParam String query) {
        try {
            List<VideoDTO> videos = videoService.searchVideosByTitle(query);
            return ResponseEntity.ok(videos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getVideosByUser(@PathVariable Long userId) {
        try {
            // This will be implemented when we have UserService method
            Map<String, String> response = new HashMap<>();
            response.put("message", "Not implemented yet");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            videoService.deleteVideo(id, currentUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Video deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }
}