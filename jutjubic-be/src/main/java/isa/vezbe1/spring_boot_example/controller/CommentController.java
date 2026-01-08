package isa.vezbe1.spring_boot_example.controller;

import isa.vezbe1.spring_boot_example.dto.CommentDTO;
import isa.vezbe1.spring_boot_example.dto.CreateCommentDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.CommentService;
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
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/videos/{videoId}/comments")
    public ResponseEntity<?> getCommentsByVideo(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
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

    @PostMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(@Valid @RequestBody CreateCommentDTO createCommentDTO) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            CommentDTO comment = commentService.createComment(createCommentDTO, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(comment);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            // Check if rate limit exceeded
            if (e.getMessage().contains("Rate limit exceeded")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @GetMapping("/videos/{videoId}/comments/count")
    public ResponseEntity<?> getCommentCount(@PathVariable Long videoId) {
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

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            commentService.deleteComment(id, currentUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Comment deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }


    @GetMapping("/comments/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyComments() {
        try {
            User currentUser = authenticationService.getCurrentUser();
            List<CommentDTO> comments = commentService.getCommentsByAuthor(currentUser);

            return ResponseEntity.ok(comments);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}