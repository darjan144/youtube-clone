package isa.vezbe1.spring_boot_example.service;

import isa.vezbe1.spring_boot_example.model.Video;
import isa.vezbe1.spring_boot_example.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ViewCountConcurrencyTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    private Video testVideo;
    private Long initialViewCount;

    @BeforeEach
    public void setup() {
        // Get existing video from import.sql
        testVideo = videoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No videos found in database. Make sure import.sql is loaded."));

        initialViewCount = testVideo.getViewCount();
        System.out.println("Test video ID: " + testVideo.getId());
        System.out.println("Initial view count: " + initialViewCount);
    }

    @Test
    public void testConcurrentViewCountIncrements() throws InterruptedException {
        System.out.println("========================================");
        System.out.println("TEST: Concurrent view count increments");
        System.out.println("========================================");

        int numberOfThreads = 50;
        int incrementsPerThread = 10;
        int totalExpectedIncrements = numberOfThreads * incrementsPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Create threads that will all start at the same time
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    // Wait for the signal to start
                    startLatch.await();

                    for (int j = 0; j < incrementsPerThread; j++) {
                        try {
                            videoService.incrementViewCount(testVideo.getId());
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            System.out.println("Thread " + threadNum + " failed: " + e.getMessage());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        System.out.println("Starting " + numberOfThreads + " threads, each incrementing " + incrementsPerThread + " times...");
        System.out.println("Expected total increments: " + totalExpectedIncrements);

        // Signal all threads to start simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        doneLatch.await();
        executor.shutdown();

        // Refresh video from database to get the updated count
        Video updatedVideo = videoRepository.findById(testVideo.getId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Long finalViewCount = updatedVideo.getViewCount();
        Long actualIncrements = finalViewCount - initialViewCount;

        System.out.println();
        System.out.println("Results:");
        System.out.println("  Initial view count: " + initialViewCount);
        System.out.println("  Final view count: " + finalViewCount);
        System.out.println("  Actual increments: " + actualIncrements);
        System.out.println("  Expected increments: " + totalExpectedIncrements);
        System.out.println("  Successful operations: " + successCount.get());
        System.out.println("  Failed operations: " + failureCount.get());

        // Verify no increments were lost
        assertEquals(totalExpectedIncrements, actualIncrements.intValue(),
                "All view count increments should be recorded (no lost updates)");
        assertEquals(0, failureCount.get(), "No operations should fail");

        System.out.println();
        System.out.println("SUCCESS: All " + totalExpectedIncrements + " concurrent increments recorded correctly!");
        System.out.println("The atomic database UPDATE prevents lost updates.");
    }

    @Test
    public void testHighConcurrencyViewCount() throws InterruptedException {
        System.out.println("========================================");
        System.out.println("TEST: High concurrency stress test");
        System.out.println("========================================");

        int numberOfThreads = 100;
        int incrementsPerThread = 20;
        int totalExpectedIncrements = numberOfThreads * incrementsPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        try {
                            videoService.incrementViewCount(testVideo.getId());
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            // Log but continue
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        System.out.println("Starting stress test: " + numberOfThreads + " threads x " + incrementsPerThread + " increments = " + totalExpectedIncrements + " total");

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long endTime = System.currentTimeMillis();

        executor.shutdown();

        Video updatedVideo = videoRepository.findById(testVideo.getId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Long finalViewCount = updatedVideo.getViewCount();
        Long actualIncrements = finalViewCount - initialViewCount;

        System.out.println();
        System.out.println("Results:");
        System.out.println("  Time taken: " + (endTime - startTime) + " ms");
        System.out.println("  Throughput: " + (totalExpectedIncrements * 1000 / (endTime - startTime)) + " ops/sec");
        System.out.println("  Initial view count: " + initialViewCount);
        System.out.println("  Final view count: " + finalViewCount);
        System.out.println("  Actual increments: " + actualIncrements);
        System.out.println("  Expected increments: " + totalExpectedIncrements);

        assertEquals(totalExpectedIncrements, actualIncrements.intValue(),
                "High concurrency: All increments should be recorded");

        System.out.println();
        System.out.println("SUCCESS: Stress test passed - atomic increments work correctly!");
    }

    @Test
    public void testMultipleVideosConcurrentViews() throws InterruptedException {
        System.out.println("========================================");
        System.out.println("TEST: Multiple videos concurrent views");
        System.out.println("========================================");

        // Get two videos if available
        var allVideos = videoRepository.findAll();
        if (allVideos.size() < 2) {
            System.out.println("Skipping test - need at least 2 videos");
            return;
        }

        Video video1 = allVideos.get(0);
        Video video2 = allVideos.get(1);
        Long initialCount1 = video1.getViewCount();
        Long initialCount2 = video2.getViewCount();

        int threadsPerVideo = 25;
        int incrementsPerThread = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadsPerVideo * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadsPerVideo * 2);

        // Threads for video 1
        for (int i = 0; i < threadsPerVideo; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        videoService.incrementViewCount(video1.getId());
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Threads for video 2
        for (int i = 0; i < threadsPerVideo; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        videoService.incrementViewCount(video2.getId());
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        System.out.println("Testing concurrent views on 2 videos simultaneously...");
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        Video updated1 = videoRepository.findById(video1.getId()).orElseThrow();
        Video updated2 = videoRepository.findById(video2.getId()).orElseThrow();

        int expectedPerVideo = threadsPerVideo * incrementsPerThread;

        System.out.println();
        System.out.println("Video 1 - Expected: " + expectedPerVideo + ", Actual: " + (updated1.getViewCount() - initialCount1));
        System.out.println("Video 2 - Expected: " + expectedPerVideo + ", Actual: " + (updated2.getViewCount() - initialCount2));

        assertEquals(expectedPerVideo, (updated1.getViewCount() - initialCount1).intValue());
        assertEquals(expectedPerVideo, (updated2.getViewCount() - initialCount2).intValue());

        System.out.println();
        System.out.println("SUCCESS: Multiple videos handle concurrent views independently!");
    }
}
