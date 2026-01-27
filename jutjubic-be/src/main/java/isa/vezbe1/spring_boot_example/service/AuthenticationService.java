package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.LoginDTO;
import isa.vezbe1.spring_boot_example.dto.UserDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.repository.UserRepository;
import isa.vezbe1.spring_boot_example.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String login(LoginDTO loginDTO) {
        // Find user by email
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if account is activated
        if (!user.isEnabled()) {
            throw new RuntimeException("Account not activated. Please check your email for activation link.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),  // Spring Security needs username (which is email in our case)
                        loginDTO.getPassword()  // Raw password
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenUtils.generateToken(user.getUsername());

        return token;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = authentication.getName();

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));
    }


    public UserDTO getCurrentUserDTO() {
        User user = getCurrentUser();
        return new UserDTO(user);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}