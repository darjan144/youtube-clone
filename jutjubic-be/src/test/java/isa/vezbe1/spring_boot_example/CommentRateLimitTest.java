package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.dto.CommentDTO;
import isa.vezbe1.spring_boot_example.dto.CreateCommentDTO;
import isa.vezbe1.spring_boot_example.model.User;
import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.repository.UserRepository;
import isa.vezbe1.spring_boot_example.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CommentRateLimitTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    private User testUser;
    private Video testVideo;

    @BeforeEach
    public void setup() {
        // Clear Redis cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Get or create a test user
        testUser = userRepository.findByEmail("test@example.com")
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail("test@example.com");
                    user.setPassword("password");
                    user.setFirstName("Test");
                    user.setLastName("User");
                    user.setEnabled(true);
                    return userRepository.save(user);
                });

        // Get or create a test video
        testVideo = videoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No videos found in database. Please add test data."));
    }

    @Test
    public void testCommentRateLimitSuccess() {
        System.out.println("========================================");
        System.out.println("TEST: Creating comments within rate limit");
        System.out.println("========================================");

        // Try to create 60 comments (should all succeed)
        for (int i = 1; i <= 60; i++) {
            CreateCommentDTO dto = new CreateCommentDTO();
            dto.setText("Test comment #" + i);
            dto.setVideoId(testVideo.getId());

            CommentDTO result = commentService.createComment(dto, testUser);
            assertNotNull(result);
            System.out.println("Comment " + i + "/60 created successfully");
        }

        System.out.println("✓ All 60 comments created successfully!");
        System.out.println();
    }

    @Test
    public void testCommentRateLimitExceeded() {
        System.out.println("========================================");
        System.out.println("TEST: Exceeding comment rate limit");
        System.out.println("========================================");

        // Create 60 comments (max allowed)
        for (int i = 1; i <= 60; i++) {
            CreateCommentDTO dto = new CreateCommentDTO();
            dto.setText("Test comment #" + i);
            dto.setVideoId(testVideo.getId());

            commentService.createComment(dto, testUser);
            System.out.println("Comment " + i + "/60 created");
        }

        System.out.println("✓ Reached maximum of 60 comments");
        System.out.println("Attempting to create comment #61...");

        // Try to create the 61st comment (should fail)
        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("This should fail - comment #61");
        dto.setVideoId(testVideo.getId());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            commentService.createComment(dto, testUser);
        });

        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        System.out.println("✓ Comment #61 correctly rejected!");
        System.out.println("Error message: " + exception.getMessage());
        System.out.println();
    }

    @Test
    public void testMultipleUsersCanCommentIndependently() {
        System.out.println("========================================");
        System.out.println("TEST: Multiple users commenting independently");
        System.out.println("========================================");

        // Create another test user
        User testUser2 = new User();
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("password");
        testUser2.setFirstName("Test2");
        testUser2.setLastName("User2");
        testUser2.setEnabled(true);  // ← CHANGED from setActivated
        testUser2 = userRepository.save(testUser2);

        // User 1 creates 60 comments
        for (int i = 1; i <= 60; i++) {
            CreateCommentDTO dto = new CreateCommentDTO();
            dto.setText("User1 comment #" + i);
            dto.setVideoId(testVideo.getId());
            commentService.createComment(dto, testUser);
        }
        System.out.println("✓ User 1 created 60 comments");

        // User 1 should be rate limited now
        CreateCommentDTO dto1 = new CreateCommentDTO();
        dto1.setText("User1 comment #61");
        dto1.setVideoId(testVideo.getId());

        assertThrows(RuntimeException.class, () -> {
            commentService.createComment(dto1, testUser);
        });
        System.out.println("✓ User 1 is correctly rate limited");

        // User 2 should still be able to comment
        CreateCommentDTO dto2 = new CreateCommentDTO();
        dto2.setText("User2 comment #1");
        dto2.setVideoId(testVideo.getId());

        CommentDTO result = commentService.createComment(dto2, testUser2);
        assertNotNull(result);
        System.out.println("✓ User 2 can still create comments (independent rate limit)");
        System.out.println();
    }

    @Test
    public void testRateLimitAcrossDifferentVideos() {
        System.out.println("========================================");
        System.out.println("TEST: Rate limit applies across all videos");
        System.out.println("========================================");

        // Get another video (or use the same one if only one exists)
        Video video2 = videoRepository.findAll().stream()
                .skip(1)
                .findFirst()
                .orElse(testVideo);

        // Create 30 comments on video 1
        for (int i = 1; i <= 30; i++) {
            CreateCommentDTO dto = new CreateCommentDTO();
            dto.setText("Comment on video 1, #" + i);
            dto.setVideoId(testVideo.getId());
            commentService.createComment(dto, testUser);
        }
        System.out.println("✓ Created 30 comments on video 1");

        // Create 30 comments on video 2
        for (int i = 1; i <= 30; i++) {
            CreateCommentDTO dto = new CreateCommentDTO();
            dto.setText("Comment on video 2, #" + i);
            dto.setVideoId(video2.getId());
            commentService.createComment(dto, testUser);
        }
        System.out.println("✓ Created 30 comments on video 2");
        System.out.println("✓ Total: 60 comments across different videos");

        // Now user should be rate limited on ANY video
        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("This should fail");
        dto.setVideoId(testVideo.getId());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            commentService.createComment(dto, testUser);
        });

        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        System.out.println("✓ Rate limit correctly applies across all videos");
        System.out.println();
    }

    @Test
    public void testConcurrentComments() throws InterruptedException {
        System.out.println("========================================");
        System.out.println("TEST: Simulating concurrent comments");
        System.out.println("========================================");

        // Create multiple threads to simulate concurrent commenting
        Thread[] threads = new Thread[10];
        int[] successCount = {0};
        int[] failureCount = {0};

        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    try {
                        CreateCommentDTO dto = new CreateCommentDTO();
                        dto.setText("Thread " + threadNum + " - Comment " + j);
                        dto.setVideoId(testVideo.getId());

                        commentService.createComment(dto, testUser);
                        synchronized (successCount) {
                            successCount[0]++;
                        }
                        System.out.println("Thread " + threadNum + " - Comment " + j + " created");
                    } catch (RuntimeException e) {
                        synchronized (failureCount) {
                            failureCount[0]++;
                        }
                        System.out.println("Thread " + threadNum + " - Comment " + j + " FAILED (rate limited)");
                    }
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println();
        System.out.println("Results:");
        System.out.println("  Successful comments: " + successCount[0]);
        System.out.println("  Failed comments (rate limited): " + failureCount[0]);
        System.out.println("  Total attempts: " + (successCount[0] + failureCount[0]));

        // Should have exactly 60 successful comments
        assertEquals(60, successCount[0], "Should have exactly 60 successful comments");
        assertEquals(40, failureCount[0], "Should have 40 failed attempts");
        System.out.println("✓ Rate limit correctly enforced under concurrent load");
    }
}