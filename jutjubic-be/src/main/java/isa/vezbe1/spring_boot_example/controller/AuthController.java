package isa.vezbe1.spring_boot_example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import isa.vezbe1.spring_boot_example.dto.LoginDTO;
import isa.vezbe1.spring_boot_example.dto.RegistrationDTO;
import isa.vezbe1.spring_boot_example.dto.UserDTO;
import isa.vezbe1.spring_boot_example.service.AuthenticationService;
import isa.vezbe1.spring_boot_example.service.RateLimiterService;
import isa.vezbe1.spring_boot_example.service.UserService;
import isa.vezbe1.spring_boot_example.util.IpAddressUtil;
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
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Authentication", description = "User registration, login, activation, and session management")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and sends an activation email")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful, activation email sent"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or email already taken")
    })
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

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token. Rate limited to 5 attempts per minute per IP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "429", description = "Too many login attempts, rate limit exceeded")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO,
                                   HttpServletRequest request) {

        // Get client IP address
        String ipAddress = IpAddressUtil.getClientIpAddress(request);

        // Check rate limit BEFORE attempting login
        if (rateLimiterService.isRateLimitExceeded(ipAddress)) {
            long timeUntilReset = rateLimiterService.getTimeUntilReset(ipAddress);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Too many login attempts. Please try again later.");
            error.put("message", "Rate limit exceeded. Maximum 5 attempts per minute.");
            error.put("retryAfterSeconds", timeUntilReset);

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
        }

        try {
            // Attempt login
            String token = authenticationService.login(loginDTO);

            // Login successful - optionally reset rate limit
            // rateLimiterService.resetRateLimit(ipAddress);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Login failed - increment rate limit counter
            int attempts = rateLimiterService.incrementLoginAttempt(ipAddress);
            int remaining = rateLimiterService.getRemainingAttempts(ipAddress);

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("attemptsRemaining", remaining);

            if (remaining == 0) {
                long timeUntilReset = rateLimiterService.getTimeUntilReset(ipAddress);
                error.put("message", "Maximum attempts reached. Please try again later.");
                error.put("retryAfterSeconds", timeUntilReset);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @Operation(summary = "Activate account", description = "Activates a user account using the token sent via email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired activation token")
    })
    @GetMapping("/activate")
    public ResponseEntity<?> activateAccount(
            @Parameter(description = "Activation token from email") @RequestParam("token") String token) {
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

    @Operation(summary = "Get current user", description = "Returns the profile of the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
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

    @Operation(summary = "Logout", description = "Logs out the current user")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authenticationService.logout();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");

        return ResponseEntity.ok(response);
    }
}