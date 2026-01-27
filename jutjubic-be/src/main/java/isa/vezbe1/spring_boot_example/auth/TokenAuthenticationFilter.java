package isa.vezbe1.spring_boot_example.auth;

import io.jsonwebtoken.ExpiredJwtException;
import isa.vezbe1.spring_boot_example.util.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private TokenUtils tokenUtils;
    private UserDetailsService userDetailsService;
    protected final Log LOGGER = LogFactory.getLog(getClass());

    public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService) {
        this.tokenUtils = tokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        LOGGER.info("========== TOKEN FILTER START ==========");
        LOGGER.info("Request: " + method + " " + requestURI);

        String username = null;
        String authToken = null;

        try {
            // 1. Extract JWT token from request
            authToken = tokenUtils.getToken(request);
            LOGGER.info("Step 1 - Token extracted: " + (authToken != null ? "YES (length: " + authToken.length() + ")" : "NO"));

            if (authToken != null) {
                LOGGER.info("Token preview: " + authToken.substring(0, Math.min(20, authToken.length())) + "...");

                // 2. Read username from token
                username = tokenUtils.getUsernameFromToken(authToken);
                LOGGER.info("Step 2 - Username from token: " + username);

                if (username != null) {
                    LOGGER.info("Step 3 - Loading user details for username: " + username);

                    // 3. Load user by username
                    UserDetails userDetails = null;
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                        LOGGER.info("Step 3 - User loaded successfully: " + userDetails.getUsername());
                        LOGGER.info("User enabled: " + userDetails.isEnabled());
                        LOGGER.info("User authorities: " + userDetails.getAuthorities());
                    } catch (Exception e) {
                        LOGGER.error("Step 3 - FAILED to load user: " + e.getMessage());
                        throw e;
                    }

                    // 4. Validate token
                    LOGGER.info("Step 4 - Validating token...");
                    boolean isValid = tokenUtils.validateToken(authToken, userDetails);
                    LOGGER.info("Step 4 - Token valid: " + isValid);

                    if (isValid) {
                        // 5. Create authentication
                        LOGGER.info("Step 5 - Creating authentication...");
                        TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                        authentication.setToken(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        LOGGER.info("Step 5 - Authentication set successfully!");
                        LOGGER.info("SecurityContext authentication: " + SecurityContextHolder.getContext().getAuthentication());
                    } else {
                        LOGGER.warn("Token validation FAILED - authentication NOT set");
                    }
                } else {
                    LOGGER.warn("Username is NULL - cannot authenticate");
                }
            } else {
                LOGGER.warn("No token found in request - skipping authentication");
            }

        } catch (ExpiredJwtException ex) {
            LOGGER.error("Token EXPIRED: " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("EXCEPTION during authentication: " + ex.getClass().getName() + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        LOGGER.info("Final SecurityContext authentication: " +
                (SecurityContextHolder.getContext().getAuthentication() != null ?
                        SecurityContextHolder.getContext().getAuthentication().getName() : "NULL"));
        LOGGER.info("========== TOKEN FILTER END ==========");

        // Continue filter chain
        chain.doFilter(request, response);
    }
}