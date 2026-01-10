package isa.vezbe1.spring_boot_example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    @Value("${rate.limit.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${rate.limit.login.duration-seconds:60}")
    private long durationSeconds;

    private static final String LOGIN_RATE_LIMIT_PREFIX = "login:ratelimit:";

    /**
     * Check if the IP address has exceeded the rate limit
     *
     * @param ipAddress The IP address to check
     * @return true if rate limit exceeded, false otherwise
     */
    public boolean isRateLimitExceeded(String ipAddress) {
        String key = LOGIN_RATE_LIMIT_PREFIX + ipAddress;
        Integer attempts = redisTemplate.opsForValue().get(key);

        logger.info("Checking rate limit for IP: {} - Current attempts: {}", ipAddress, attempts);

        if (attempts == null) {
            return false;
        }

        return attempts >= maxAttempts;
    }

    /**
     * Increment the login attempt counter for an IP address
     *
     * @param ipAddress The IP address to increment
     * @return The new attempt count
     */
    public int incrementLoginAttempt(String ipAddress) {
        String key = LOGIN_RATE_LIMIT_PREFIX + ipAddress;
        Integer attempts = redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            // First attempt - set to 1 with expiration
            redisTemplate.opsForValue().set(key, 1, durationSeconds, TimeUnit.SECONDS);
            logger.warn("First failed login attempt for IP: {}", ipAddress);
            return 1;
        } else {
            // Increment existing counter
            Long newAttempts = redisTemplate.opsForValue().increment(key);
            int attemptCount = newAttempts != null ? newAttempts.intValue() : attempts + 1;
            logger.warn("Failed login attempt {} for IP: {}", attemptCount, ipAddress);
            return attemptCount;
        }
    }

    /**
     * Get remaining attempts for an IP address
     *
     * @param ipAddress The IP address to check
     * @return Number of remaining attempts
     */
    public int getRemainingAttempts(String ipAddress) {
        String key = LOGIN_RATE_LIMIT_PREFIX + ipAddress;
        Integer attempts = redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            return maxAttempts;
        }

        return Math.max(0, maxAttempts - attempts);
    }

    /**
     * Get time until rate limit reset
     *
     * @param ipAddress The IP address to check
     * @return Seconds until reset, or 0 if no limit
     */
    public long getTimeUntilReset(String ipAddress) {
        String key = LOGIN_RATE_LIMIT_PREFIX + ipAddress;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * Reset the rate limit for an IP address (for testing or admin purposes)
     *
     * @param ipAddress The IP address to reset
     */
    public void resetRateLimit(String ipAddress) {
        String key = LOGIN_RATE_LIMIT_PREFIX + ipAddress;
        redisTemplate.delete(key);
        logger.info("Rate limit reset for IP: {}", ipAddress);
    }
}