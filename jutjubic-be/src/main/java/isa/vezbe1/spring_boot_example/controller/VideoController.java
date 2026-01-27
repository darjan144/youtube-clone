package isa.vezbe1.spring_boot_example.controller;

import isa.vezbe1.spring_boot_example.dto.CreateVideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoUploadDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadVideo(
            @RequestPart("video") MultipartFile videoFile,
            @RequestPart("thumbnail") MultipartFile thumbnailFile,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "location", required = false) String location) {
        try {
            User currentUser = authenticationService.getCurrentUser();

            VideoUploadDTO uploadDTO = new VideoUploadDTO();
            uploadDTO.setTitle(title);
            uploadDTO.setDescription(description);
            uploadDTO.setTags(tags);
            uploadDTO.setLocation(location);

            VideoDTO video = videoService.uploadVideo(videoFile, thumbnailFile, uploadDTO, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(video);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
            error.put("error", "Failed to search videos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long id) {
        try {
            byte[] thumbnail = videoService.getCachedThumbnail(id);
            if (thumbnail != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(thumbnail);
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}