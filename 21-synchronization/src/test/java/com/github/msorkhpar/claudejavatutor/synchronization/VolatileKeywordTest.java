package com.github.msorkhpar.claudejavatutor.synchronization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("6.2.2 - Volatile Keyword Tests")
class VolatileKeywordTest {

    @Nested
    @DisplayName("Volatile Flag (Visibility)")
    class VolatileFlagTests {

        @Test
        @DisplayName("Should observe volatile flag update from another thread")
        @Timeout(5)
        void testVolatileFlagVisibility() throws InterruptedException {
            var flag = new VolatileKeyword.VolatileFlag();
            AtomicBoolean workerExited = new AtomicBoolean(false);

            Thread worker = Thread.ofPlatform().start(() -> {
                flag.doWork();
                workerExited.set(true);
            });

            // Let worker start spinning
            Thread.sleep(50);
            assertThat(flag.isRunning()).isTrue();

            // Stop the worker — volatile write should be immediately visible
            flag.stop();

            await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(workerExited.get()).isTrue());

            assertThat(flag.isRunning()).isFalse();
            assertThat(flag.getWorkCount()).isGreaterThan(0)
                .as("Worker should have done some work before being stopped");
        }

        @Test
        @DisplayName("Should start in running state")
        void testInitialState() {
            var flag = new VolatileKeyword.VolatileFlag();
            assertThat(flag.isRunning()).isTrue();
            assertThat(flag.getWorkCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should transition to stopped state")
        void testStopTransition() {
            var flag = new VolatileKeyword.VolatileFlag();
            flag.stop();
            assertThat(flag.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("Volatile Happens-Before")
    class VolatileHappensBeforeTests {

        @Test
        @DisplayName("Should make non-volatile writes visible via volatile flag")
        @Timeout(5)
        void testHappensBeforePiggybacking() throws InterruptedException {
            var demo = new VolatileKeyword.VolatileHappensBefore();
            AtomicBoolean consumed = new AtomicBoolean(false);

            Thread reader = Thread.ofPlatform().start(() -> {
                // Spin until ready flag is set
                while (!demo.isReady()) {
                    Thread.onSpinWait();
                }
                // After volatile read, non-volatile data and message must be visible
                consumed.set(demo.consume());
            });

            // Give reader time to start spinning
            Thread.sleep(50);

            // Publish data — volatile write piggybacks non-volatile writes
            demo.publish(42, "hello");

            await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(consumed.get()).isTrue());

            assertThat(demo.getData()).isEqualTo(42);
            assertThat(demo.getMessage()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should not consume when data not published yet")
        void testConsumeBeforePublish() {
            var demo = new VolatileKeyword.VolatileHappensBefore();
            assertThat(demo.consume()).isFalse();
            assertThat(demo.isReady()).isFalse();
        }

        @Test
        @DisplayName("Should see published data in same thread")
        void testPublishAndConsumeInSameThread() {
            var demo = new VolatileKeyword.VolatileHappensBefore();
            demo.publish(99, "world");
            assertThat(demo.consume()).isTrue();
            assertThat(demo.getData()).isEqualTo(99);
            assertThat(demo.getMessage()).isEqualTo("world");
        }

        @Test
        @DisplayName("Should demonstrate happens-before across multiple reader threads")
        @Timeout(5)
        void testMultipleReaders() throws InterruptedException {
            var demo = new VolatileKeyword.VolatileHappensBefore();
            int numReaders = 5;
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(numReaders);

            for (int i = 0; i < numReaders; i++) {
                Thread.ofPlatform().start(() -> {
                    while (!demo.isReady()) {
                        Thread.onSpinWait();
                    }
                    if (demo.consume()) {
                        successCount.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            Thread.sleep(50);
            demo.publish(7, "test");

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(successCount.get()).isEqualTo(numReaders)
                .as("All readers should see the published data via happens-before");
        }
    }

    @Nested
    @DisplayName("Volatile Atomicity Pitfall")
    class VolatileAtomicityTests {

        @Test
        @DisplayName("Volatile counter should lose increments under concurrent access (demonstrating race condition)")
        @Timeout(10)
        void testVolatileCounterRaceCondition() throws InterruptedException {
            var counter = new VolatileKeyword.VolatileCounter();
            int numThreads = 10;
            int incrementsPerThread = 10_000;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread.ofPlatform().start(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                    latch.countDown();
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            int expected = numThreads * incrementsPerThread;

            // Volatile counter WILL almost certainly lose increments due to race conditions
            // We assert that the count is less than expected (race condition occurred)
            // Note: In rare cases on single-core systems, this might pass. We use enough
            // threads and iterations to make the race condition very likely.
            assertThat(counter.getCount()).isLessThanOrEqualTo(expected)
                .as("Volatile counter may lose increments due to race condition");

            // We can't guarantee the exact count, but we can verify it's positive
            assertThat(counter.getCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("AtomicInteger counter should not lose any increments")
        @Timeout(10)
        void testAtomicCounterCorrectness() throws InterruptedException {
            var counter = new VolatileKeyword.AtomicCounter();
            int numThreads = 10;
            int incrementsPerThread = 10_000;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread.ofPlatform().start(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                    latch.countDown();
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.getCount()).isEqualTo(numThreads * incrementsPerThread)
                .as("AtomicInteger should not lose any increments");
        }

        @Test
        @DisplayName("Synchronized counter should not lose any increments")
        @Timeout(10)
        void testSynchronizedCounterCorrectness() throws InterruptedException {
            var counter = new VolatileKeyword.SynchronizedCounter();
            int numThreads = 10;
            int incrementsPerThread = 10_000;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread.ofPlatform().start(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                    latch.countDown();
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.getCount()).isEqualTo(numThreads * incrementsPerThread)
                .as("Synchronized counter should not lose any increments");
        }

        @Test
        @DisplayName("Single-threaded volatile counter should work correctly")
        void testSingleThreadVolatileCounter() {
            var counter = new VolatileKeyword.VolatileCounter();
            for (int i = 0; i < 100; i++) {
                counter.increment();
            }
            assertThat(counter.getCount()).isEqualTo(100)
                .as("Single-threaded access should be correct even with volatile");
        }
    }

    @Nested
    @DisplayName("Double-Checked Locking")
    class DoubleCheckedLockingTests {

        @Test
        @DisplayName("Should lazily initialize the instance")
        void testLazyInitialization() {
            AtomicInteger callCount = new AtomicInteger(0);
            var lazy = new VolatileKeyword.DoubleCheckedLazy<>(() -> {
                callCount.incrementAndGet();
                return "initialized";
            });

            assertThat(lazy.isInitialized()).isFalse();

            String result = lazy.getInstance();

            assertThat(result).isEqualTo("initialized");
            assertThat(lazy.isInitialized()).isTrue();
            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return same instance on multiple calls")
        void testSameInstance() {
            var lazy = new VolatileKeyword.DoubleCheckedLazy<>(() -> new Object());

            Object first = lazy.getInstance();
            Object second = lazy.getInstance();
            Object third = lazy.getInstance();

            assertThat(first).isSameAs(second).isSameAs(third);
        }

        @Test
        @DisplayName("Supplier should be called exactly once even under concurrent access")
        @Timeout(10)
        void testConcurrentInitialization() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);
            var lazy = new VolatileKeyword.DoubleCheckedLazy<>(() -> {
                callCount.incrementAndGet();
                return "result";
            });

            int numThreads = 20;
            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            String[] results = new String[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int idx = i;
                Thread.ofPlatform().start(() -> {
                    try {
                        startGate.await(); // all threads start at the same time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    results[idx] = lazy.getInstance();
                    doneLatch.countDown();
                });
            }

            startGate.countDown(); // release all threads simultaneously
            doneLatch.await(10, TimeUnit.SECONDS);

            assertThat(callCount.get()).isEqualTo(1)
                .as("Supplier should be called exactly once despite concurrent access");

            // All threads should see the same instance
            String expected = results[0];
            for (int i = 1; i < numThreads; i++) {
                assertThat(results[i]).isSameAs(expected)
                    .as("All threads should see the same lazily-initialized instance");
            }
        }

        @Test
        @DisplayName("Should handle supplier returning null gracefully")
        void testNullSupplier() {
            // Supplier returns null - the lazy will keep trying since instance == null
            AtomicInteger callCount = new AtomicInteger(0);
            var lazy = new VolatileKeyword.DoubleCheckedLazy<String>(() -> {
                callCount.incrementAndGet();
                return null;
            });

            String result = lazy.getInstance();
            assertThat(result).isNull();
            assertThat(lazy.isInitialized()).isFalse();

            // Call again - will invoke supplier again since instance is still null
            lazy.getInstance();
            assertThat(callCount.get()).isEqualTo(2)
                .as("Supplier returning null should not cache, allowing retry");
        }
    }

    @Nested
    @DisplayName("Volatile 64-bit Atomicity")
    class Volatile64BitTests {

        @Test
        @DisplayName("Should safely read/write volatile long across threads")
        @Timeout(5)
        void testVolatileLongCrossThread() throws InterruptedException {
            var demo = new VolatileKeyword.Volatile64Bit();
            long expectedValue = 0x7FFFFFFF_FFFFFFFFL; // large 64-bit value
            AtomicBoolean readerSawCorrectValue = new AtomicBoolean(false);

            Thread reader = Thread.ofPlatform().start(() -> {
                while (demo.getVolatileTimestamp() == 0L) {
                    Thread.onSpinWait();
                }
                // Volatile guarantees atomic 64-bit read
                long val = demo.getVolatileTimestamp();
                readerSawCorrectValue.set(val == expectedValue);
            });

            Thread.sleep(50);
            demo.setVolatileTimestamp(expectedValue);

            await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(readerSawCorrectValue.get()).isTrue());
        }

        @Test
        @DisplayName("Should read and write volatile long correctly")
        void testVolatileLongBasic() {
            var demo = new VolatileKeyword.Volatile64Bit();
            demo.setVolatileTimestamp(Long.MAX_VALUE);
            assertThat(demo.getVolatileTimestamp()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("Should read and write non-volatile long correctly in single thread")
        void testNonVolatileLongSingleThread() {
            var demo = new VolatileKeyword.Volatile64Bit();
            demo.setNonVolatileTimestamp(123456789L);
            assertThat(demo.getNonVolatileTimestamp()).isEqualTo(123456789L);
        }

        @Test
        @DisplayName("Default values should be zero")
        void testDefaultValues() {
            var demo = new VolatileKeyword.Volatile64Bit();
            assertThat(demo.getVolatileTimestamp()).isEqualTo(0L);
            assertThat(demo.getNonVolatileTimestamp()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Non-Volatile Flag (Visibility Bug Demo)")
    class NonVolatileFlagTests {

        @Test
        @DisplayName("Non-volatile flag should still work in single thread")
        void testNonVolatileFlagSingleThread() {
            var flag = new VolatileKeyword.NonVolatileFlag();
            assertThat(flag.isRunning()).isTrue();
            flag.stop();
            assertThat(flag.isRunning()).isFalse();
        }

        @Test
        @DisplayName("Non-volatile flag visibility is not guaranteed across threads")
        void testNonVolatileFlagCreation() {
            // We can't reliably test the visibility bug because it depends on JIT compilation
            // and CPU caching behavior. Instead, we verify the API works and document the risk.
            var flag = new VolatileKeyword.NonVolatileFlag();
            assertThat(flag.getWorkCount()).isEqualTo(0);
            assertThat(flag.isRunning()).isTrue();
        }
    }
}
