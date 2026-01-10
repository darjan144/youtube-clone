package isa.vezbe1.spring_boot_example.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpAddressUtil {

    /**
     * Extract client IP address from request
     * Handles X-Forwarded-For header for proxied requests
     *
     * @param request The HTTP request
     * @return The client IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header first (for proxied requests)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }
}