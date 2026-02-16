package isa.vezbe1.spring_boot_example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import isa.vezbe1.spring_boot_example.dto.CommentDTO;
import isa.vezbe1.spring_boot_example.dto.CreateVideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoDTO;
import isa.vezbe1.spring_boot_example.dto.VideoUploadDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.CommentService;
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
@Tag(name = "Videos", description = "Video CRUD, upload, search, thumbnails, and video comments")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthenticationService authenticationService;

    @Operation(summary = "Get all videos", description = "Returns a paginated list of all videos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Videos retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllVideos(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
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

    @Operation(summary = "Get video by ID", description = "Returns a single video by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Video found"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getVideoById(@Parameter(description = "Video ID") @PathVariable Long id) {
        try {
            VideoDTO video = videoService.getVideoById(id);
            return ResponseEntity.ok(video);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @Operation(summary = "Upload a video", description = "Uploads a video file with thumbnail. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadVideo(
            @Parameter(description = "Video file") @RequestPart("video") MultipartFile videoFile,
            @Parameter(description = "Thumbnail image") @RequestPart("thumbnail") MultipartFile thumbnailFile,
            @Parameter(description = "Video title") @RequestParam("title") String title,
            @Parameter(description = "Video description") @RequestParam("description") String description,
            @Parameter(description = "Video tags") @RequestParam(value = "tags", required = false) List<String> tags,
            @Parameter(description = "Video location") @RequestParam(value = "location", required = false) String location) {
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

    @Operation(summary = "Create video metadata", description = "Creates a video entry with metadata. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Video created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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

    @Operation(summary = "Increment view count", description = "Increments the view count of a video by one")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "View count incremented"),
            @ApiResponse(responseCode = "400", description = "Video not found")
    })
    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementViewCount(@Parameter(description = "Video ID") @PathVariable Long id) {
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

    @Operation(summary = "Search videos", description = "Searches videos by title")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "500", description = "Search failed")
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchVideos(@Parameter(description = "Search query") @RequestParam String query) {
        try {
            List<VideoDTO> videos = videoService.searchVideosByTitle(query);
            return ResponseEntity.ok(videos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search videos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Get video thumbnail", description = "Returns the cached thumbnail image for a video")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thumbnail returned"),
            @ApiResponse(responseCode = "404", description = "Thumbnail not found"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve thumbnail")
    })
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@Parameter(description = "Video ID") @PathVariable Long id) {
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

    // ========== COMMENTS ENDPOINTS (moved here to avoid routing conflicts) ==========

    @Operation(summary = "Get comments for video", description = "Returns a paginated list of comments for a specific video")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments retrieved"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @GetMapping("/{videoId}/comments")
    public ResponseEntity<?> getCommentsByVideo(
            @Parameter(description = "Video ID") @PathVariable Long videoId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page < 0 || size <= 0 || size > 100) {
                Pageable pageable = PageRequest.of(0, 20);
                Page<CommentDTO> comments = commentService.getCommentsByVideoId(videoId, pageable);
                return ResponseEntity.ok(comments);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<CommentDTO> comments = commentService.getCommentsByVideoId(videoId, pageable);

            return ResponseEntity.ok(comments);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Get comment count", description = "Returns the number of comments on a video")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment count returned"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @GetMapping("/{videoId}/comments/count")
    public ResponseEntity<?> getCommentCount(@Parameter(description = "Video ID") @PathVariable Long videoId) {
        try {
            Long count = commentService.getCommentCount(videoId);

            Map<String, Long> response = new HashMap<>();
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}