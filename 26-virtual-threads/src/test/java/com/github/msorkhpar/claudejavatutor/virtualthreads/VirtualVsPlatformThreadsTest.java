package com.github.msorkhpar.claudejavatutor.virtualthreads;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Virtual vs Platform Threads Tests")
class VirtualVsPlatformThreadsTest {

    @Nested
    @DisplayName("Scalability and Resource Consumption")
    class ScalabilityTest {

        @Test
        @DisplayName("Should scale to thousands of virtual threads")
        void testScaleVirtualThreads() throws InterruptedException {
            int count = 5000;
            int completed = VirtualVsPlatformThreads.scaleVirtualThreads(count);
            assertThat(completed).isEqualTo(count);
        }

        @Test
        @DisplayName("Should handle single virtual thread")
        void testScaleSingleVirtualThread() throws InterruptedException {
            int completed = VirtualVsPlatformThreads.scaleVirtualThreads(1);
            assertThat(completed).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle zero threads")
        void testScaleZeroThreads() throws InterruptedException {
            int completed = VirtualVsPlatformThreads.scaleVirtualThreads(0);
            assertThat(completed).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("I/O Performance Comparison")
    class IOPerformanceTest {

        @Test
        @DisplayName("Virtual threads should outperform limited thread pool for I/O tasks")
        void testVirtualThreadsOutperformFixedPool() throws InterruptedException {
            int taskCount = 200;
            long sleepMillis = 50;
            int poolSize = 10;

            long virtualTime = VirtualVsPlatformThreads.measureVirtualThreadIOPerformance(
                    taskCount, sleepMillis);
            long platformTime = VirtualVsPlatformThreads.measurePlatformThreadIOPerformance(
                    taskCount, poolSize, sleepMillis);

            // Virtual threads should be significantly faster for I/O-bound tasks
            // because they all run concurrently, while the fixed pool is limited to poolSize
            assertThat(virtualTime).isLessThan(platformTime);
        }

        @Test
        @DisplayName("Virtual threads should complete I/O tasks concurrently")
        void testVirtualThreadsConcurrency() throws InterruptedException {
            int taskCount = 100;
            long sleepMillis = 100;

            long elapsed = VirtualVsPlatformThreads.measureVirtualThreadIOPerformance(
                    taskCount, sleepMillis);

            // If truly concurrent, 100 tasks sleeping 100ms should take ~100ms, not 10000ms
            assertThat(elapsed).isLessThan(3000);
        }
    }

    @Nested
    @DisplayName("Carrier Thread Sharing")
    class CarrierThreadSharingTest {

        @Test
        @DisplayName("Should demonstrate carrier thread sharing among virtual threads")
        void testCarrierThreadSharing() throws InterruptedException {
            List<String> carrierNames = VirtualVsPlatformThreads.demonstrateCarrierThreadSharing(20);

            // Should have entries (2 per task)
            assertThat(carrierNames).isNotEmpty();
            assertThat(carrierNames).hasSize(40);
        }
    }

    @Nested
    @DisplayName("Pinning with Synchronized vs ReentrantLock")
    class PinningTest {

        @Test
        @DisplayName("Should work correctly with synchronized blocks (even if pinning occurs)")
        void testSynchronizedPinning() throws InterruptedException {
            var demo = new VirtualVsPlatformThreads.PinningDemonstration();
            int taskCount = 20;

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new java.util.ArrayList<>();
                for (int i = 0; i < taskCount; i++) {
                    futures.add(executor.submit(demo::incrementWithSynchronized));
                }
                for (Future<?> f : futures) {
                    try {
                        f.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException | TimeoutException e) {
                        fail("Task failed: " + e.getMessage());
                    }
                }
            }

            assertThat(demo.getCount()).isEqualTo(taskCount);
        }

        @Test
        @DisplayName("Should work correctly with ReentrantLock (no pinning)")
        void testReentrantLockNoPinning() throws InterruptedException {
            var demo = new VirtualVsPlatformThreads.PinningDemonstration();
            int taskCount = 20;

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new java.util.ArrayList<>();
                for (int i = 0; i < taskCount; i++) {
                    futures.add(executor.submit(demo::incrementWithLock));
                }
                for (Future<?> f : futures) {
                    try {
                        f.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException | TimeoutException e) {
                        fail("Task failed: " + e.getMessage());
                    }
                }
            }

            assertThat(demo.getCount()).isEqualTo(taskCount);
        }

        @Test
        @DisplayName("Should reset counter correctly")
        void testResetCount() {
            var demo = new VirtualVsPlatformThreads.PinningDemonstration();
            demo.incrementWithLock();
            assertThat(demo.getCount()).isEqualTo(1);
            demo.resetCount();
            assertThat(demo.getCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Compatibility with Existing APIs")
    class CompatibilityTest {

        @Test
        @DisplayName("Should work with Callable and Future")
        void testCallableAndFuture() throws ExecutionException, InterruptedException {
            String result = VirtualVsPlatformThreads.CompatibilityExamples.submitCallable(
                    () -> "hello from virtual thread");
            assertThat(result).isEqualTo("hello from virtual thread");
        }

        @Test
        @DisplayName("Should work with Callable returning integer")
        void testCallableWithInteger() throws ExecutionException, InterruptedException {
            int result = VirtualVsPlatformThreads.CompatibilityExamples.submitCallable(
                    () -> 42);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should propagate exceptions from Callable")
        void testCallableException() {
            assertThatThrownBy(() ->
                    VirtualVsPlatformThreads.CompatibilityExamples.submitCallable(() -> {
                        throw new IllegalStateException("test error");
                    })
            ).isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should coordinate with CountDownLatch")
        void testCoordinateWithLatch() throws InterruptedException {
            int sum = VirtualVsPlatformThreads.CompatibilityExamples.coordinateWithLatch(10);
            // Sum of 0+1+2+...+9 = 45
            assertThat(sum).isEqualTo(45);
        }

        @Test
        @DisplayName("Should handle zero threads with CountDownLatch")
        void testCoordinateWithLatchZero() throws InterruptedException {
            int sum = VirtualVsPlatformThreads.CompatibilityExamples.coordinateWithLatch(0);
            assertThat(sum).isEqualTo(0);
        }

        @Test
        @DisplayName("Should work with CompletableFuture")
        void testCompletableFuture() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<String> future = VirtualVsPlatformThreads.CompatibilityExamples
                    .runWithCompletableFuture("hello");
            String result = future.get(5, TimeUnit.SECONDS);
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should work with producer-consumer pattern")
        void testProducerConsumer() throws InterruptedException {
            List<String> consumed = VirtualVsPlatformThreads.CompatibilityExamples
                    .producerConsumerWithVirtualThreads(10);
            assertThat(consumed).hasSize(10);
            assertThat(consumed).allMatch(s -> s.startsWith("item-"));
        }

        @Test
        @DisplayName("Should report VirtualThreads thread group")
        void testVirtualThreadGroup() {
            String groupName = VirtualVsPlatformThreads.CompatibilityExamples.getVirtualThreadGroup();
            assertThat(groupName).isEqualTo("VirtualThreads");
        }

        @Test
        @DisplayName("Should always be daemon thread")
        void testVirtualThreadAlwaysDaemon() {
            assertThat(VirtualVsPlatformThreads.CompatibilityExamples.virtualThreadAlwaysDaemon()).isTrue();
        }

        @Test
        @DisplayName("Should throw when setting daemon to false on virtual thread")
        void testSetDaemonFalseThrows() {
            assertThatThrownBy(VirtualVsPlatformThreads.CompatibilityExamples::setDaemonFalseOnVirtualThread)
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should have fixed NORM_PRIORITY")
        void testVirtualThreadFixedPriority() {
            assertThat(VirtualVsPlatformThreads.CompatibilityExamples.virtualThreadFixedPriority())
                    .isEqualTo(Thread.NORM_PRIORITY);
        }

        @Test
        @DisplayName("Should ignore setPriority on virtual thread")
        void testSetPriorityIgnored() {
            int priority = VirtualVsPlatformThreads.CompatibilityExamples
                    .setPriorityOnVirtualThread(Thread.MAX_PRIORITY);
            assertThat(priority).isEqualTo(Thread.NORM_PRIORITY);
        }
    }
}
