package isa.vezbe1.spring_boot_example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import isa.vezbe1.spring_boot_example.dto.CommentDTO;
import isa.vezbe1.spring_boot_example.dto.CreateCommentDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "Comments", description = "Comment creation, deletion, and retrieval")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthenticationService authenticationService;

    // Note: GET /videos/{videoId}/comments moved to VideoController to avoid routing conflicts

    @Operation(summary = "Create a comment", description = "Posts a new comment on a video. Rate limited to 60 comments/hour per user. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "429", description = "Comment rate limit exceeded")
    })
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


    // Note: GET /videos/{videoId}/comments/count moved to VideoController to avoid routing conflicts

    @Operation(summary = "Delete a comment", description = "Deletes a comment by ID. Only the comment author can delete it. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not the comment author")
    })
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@Parameter(description = "Comment ID") @PathVariable Long id) {
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


    @Operation(summary = "Get my comments", description = "Returns all comments by the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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