package isa.vezbe1.spring_boot_example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import isa.vezbe1.spring_boot_example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TestController - For testing email and other configurations
 * DELETE THIS CONTROLLER IN PRODUCTION!
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Test", description = "Development/testing endpoints (not for production)")
public class TestController {

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Test email sending", description = "Sends a test activation email. For development only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email sent"),
            @ApiResponse(responseCode = "500", description = "Failed to send email")
    })
    @GetMapping("/email")
    public ResponseEntity<?> testEmail(@Parameter(description = "Recipient email address") @RequestParam String to) {
        try {
            // Send test activation email
            String testToken = "test-token-12345";
            emailService.sendActivationEmail(to, testToken);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Test email sent successfully to: " + to);
            response.put("activationLink", "http://localhost:8084/api/auth/activate?token=" + testToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send email: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @Operation(summary = "Health check", description = "Returns OK if the backend is running")
    @ApiResponse(responseCode = "200", description = "Backend is healthy")
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Backend is running");
        return ResponseEntity.ok(response);
    }
}