package isa.vezbe1.spring_boot_example.controller;

import isa.vezbe1.spring_boot_example.dto.LoginDTO;
import isa.vezbe1.spring_boot_example.dto.RegistrationDTO;
import isa.vezbe1.spring_boot_example.dto.UserDTO;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // React frontend URL
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDTO registrationDTO) {
        try {
            UserDTO user = userService.registerUser(registrationDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful! Please check your email to activate your account.");
            response.put("user", user);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO,
                                   HttpServletRequest request) {
        try {
            // TODO: Add IP rate limiting here (with Redis)
            // String ipAddress = getClientIpAddress(request);
            // Check if IP has exceeded 5 attempts per minute

            String token = authenticationService.login(loginDTO);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestParam("token") String token) {
        try {
            userService.activateAccount(token);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Account activated successfully! You can now login.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            UserDTO user = authenticationService.getCurrentUserDTO();
            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authenticationService.logout();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");

        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs, take the first one
        return xForwardedForHeader.split(",")[0].trim();
    }
}