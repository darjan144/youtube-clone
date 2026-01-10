package isa.vezbe1.spring_boot_example.controller;

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
public class TestController {

    @Autowired
    private EmailService emailService;

    /**
     * Test email configuration
     *
     * DELETE THIS ENDPOINT IN PRODUCTION!
     *
     * Usage: GET /api/test/email?to=your-email@example.com
     */
    @GetMapping("/email")
    public ResponseEntity<?> testEmail(@RequestParam String to) {
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

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Backend is running");
        return ResponseEntity.ok(response);
    }
}