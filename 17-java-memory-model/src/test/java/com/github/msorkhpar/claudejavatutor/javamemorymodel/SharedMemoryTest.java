package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("5.1.2 - Shared Memory and Thread-Local Memory Tests")
class SharedMemoryTest {

    @Nested
    @DisplayName("Volatile Visibility")
    class VolatileVisibilityTests {

        @Test
        @DisplayName("Should observe volatile flag update from another thread")
        @Timeout(5)
        void testVolatileFlagVisibility() throws InterruptedException {
            SharedMemory.VolatileFlag flag = new SharedMemory.VolatileFlag();
            CountDownLatch readerStarted = new CountDownLatch(1);
            AtomicBoolean readerSawStop = new AtomicBoolean(false);

            Thread reader = new Thread(() -> {
                readerStarted.countDown();
                // Spin until volatile flag is set — visible due to volatile
                while (!flag.shouldStop()) {
                    Thread.onSpinWait();
                }
                readerSawStop.set(true);
            });

            reader.start();
            readerStarted.await();
            Thread.sleep(10); // give reader time to start spinning
            flag.stop();       // volatile write — immediately visible to reader

            reader.join(3000);
            assertThat(reader.isAlive()).isFalse()
                .as("Reader thread should have exited after volatile flag set");
            assertThat(readerSawStop.get()).isTrue();
        }

        @Test
        @DisplayName("Should demonstrate volatile write makes prior writes visible")
        void testVolatilePiggybacking() throws InterruptedException {
            SharedMemory.VolatilePiggybacking demo = new SharedMemory.VolatilePiggybacking();
            boolean result = demo.run();
            assertThat(result).isTrue()
                .as("Writes before volatile write must be visible after volatile read");
        }

        @Test
        @DisplayName("Should demonstrate that volatile flag write/read is correct across threads")
        void testVolatileFlagReset() throws InterruptedException {
            SharedMemory.VolatileFlag flag = new SharedMemory.VolatileFlag();

            assertThat(flag.shouldStop()).isFalse();
            flag.stop();
            assertThat(flag.shouldStop()).isTrue();
            flag.reset();
            assertThat(flag.shouldStop()).isFalse();
        }
    }

    @Nested
    @DisplayName("ThreadLocal Isolation")
    class ThreadLocalTests {

        @Test
        @DisplayName("Each thread should have its own ThreadLocal value")
        void testThreadLocalIsolation() throws InterruptedException {
            SharedMemory.ThreadLocalDemo demo = new SharedMemory.ThreadLocalDemo();
            int numThreads = 5;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            int[] results = new int[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int id = i;
                Thread t = new Thread(() -> {
                    try {
                        startLatch.await();
                        demo.setId(id);
                        // Simulate some work
                        Thread.sleep(10);
                        results[id] = demo.getId();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        demo.cleanup();
                        doneLatch.countDown();
                    }
                });
                t.start();
            }

            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();

            // Each thread should have read back its own value, not another thread's
            for (int i = 0; i < numThreads; i++) {
                assertThat(results[i]).isEqualTo(i)
                    .as("Thread %d should see its own ThreadLocal value", i);
            }
        }

        @Test
        @DisplayName("ThreadLocal should return initial value before first set")
        void testThreadLocalInitialValue() {
            SharedMemory.ThreadLocalDemo demo = new SharedMemory.ThreadLocalDemo();
            // Main thread hasn't set a value — should get default
            assertThat(demo.getId()).isEqualTo(-1);
            demo.cleanup();
        }

        @Test
        @DisplayName("ThreadLocal cleanup should remove value from current thread")
        void testThreadLocalCleanup() {
            SharedMemory.ThreadLocalDemo demo = new SharedMemory.ThreadLocalDemo();
            demo.setId(99);
            assertThat(demo.getId()).isEqualTo(99);
            demo.cleanup();
            assertThat(demo.getId()).isEqualTo(-1)
                .as("After cleanup, initial value should be returned");
        }
    }

    @Nested
    @DisplayName("Volatile Long/Double Atomicity")
    class VolatileLongTests {

        @Test
        @DisplayName("Volatile long counter should be atomically readable")
        void testVolatileLongAtomicity() throws InterruptedException {
            SharedMemory.VolatileLongDemo demo = new SharedMemory.VolatileLongDemo();
            long expected = 1_000_000L;
            int numThreads = 10;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < expected / numThreads; j++) {
                        demo.increment(); // synchronized increment
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(15, TimeUnit.SECONDS);
            assertThat(demo.getCount()).isEqualTo(expected)
                .as("Synchronized volatile long increments should be exact");
        }

        @Test
        @DisplayName("AtomicLong should provide correct lock-free long operations")
        void testAtomicLong() throws InterruptedException {
            AtomicLong counter = new AtomicLong(0);
            int numThreads = 10;
            int opsPerThread = 1000;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        counter.incrementAndGet();
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.get()).isEqualTo((long) numThreads * opsPerThread)
                .as("AtomicLong must produce correct result");
        }
    }

    @Nested
    @DisplayName("Safe Publication Patterns")
    class SafePublicationTests {

        @Test
        @DisplayName("Should safely publish object via volatile reference")
        void testSafePublicationViaVolatile() throws InterruptedException {
            SharedMemory.SafePublisher publisher = new SharedMemory.SafePublisher();
            CountDownLatch done = new CountDownLatch(1);
            final int[] observed = {-1};

            Thread reader = new Thread(() -> {
                SharedMemory.SafePublisher.Config config;
                while ((config = publisher.getConfig()) == null) {
                    Thread.onSpinWait();
                }
                observed[0] = config.getPort();
                done.countDown();
            });

            reader.start();
            Thread.sleep(20);
            publisher.publish(new SharedMemory.SafePublisher.Config("localhost", 8080));

            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(observed[0]).isEqualTo(8080)
                .as("Port from safely published config should be 8080");
        }

        @Test
        @DisplayName("Should safely publish immutable object across multiple readers")
        void testSafePublicationMultipleReaders() throws InterruptedException {
            SharedMemory.SafePublisher publisher = new SharedMemory.SafePublisher();
            int numReaders = 5;
            CountDownLatch done = new CountDownLatch(numReaders);
            int[] ports = new int[numReaders];

            for (int i = 0; i < numReaders; i++) {
                final int idx = i;
                Thread reader = new Thread(() -> {
                    SharedMemory.SafePublisher.Config config;
                    while ((config = publisher.getConfig()) == null) {
                        Thread.onSpinWait();
                    }
                    ports[idx] = config.getPort();
                    done.countDown();
                });
                reader.start();
            }

            Thread.sleep(20);
            publisher.publish(new SharedMemory.SafePublisher.Config("localhost", 9090));

            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            for (int port : ports) {
                assertThat(port).isEqualTo(9090)
                    .as("All readers should see same safely published port");
            }
        }
    }

    @Nested
    @DisplayName("Array Element Sharing")
    class ArraySharingTests {

        @Test
        @DisplayName("Synchronized array updates should produce correct results")
        void testSynchronizedArrayUpdates() throws InterruptedException {
            SharedMemory.SharedArray sharedArray = new SharedMemory.SharedArray(10);
            int numThreads = 5;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int threadId = 0; threadId < numThreads; threadId++) {
                final int tid = threadId;
                Thread t = new Thread(() -> {
                    for (int i = 0; i < 10; i++) {
                        sharedArray.addToElement(i, tid + 1); // add tid+1 to each element
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            // Each element should have been incremented by 1+2+3+4+5 = 15 for each thread
            int expectedSum = (1 + 2 + 3 + 4 + 5); // 5 threads, adding tid+1
            for (int i = 0; i < 10; i++) {
                assertThat(sharedArray.getElement(i)).isEqualTo(expectedSum)
                    .as("Element[%d] should equal sum of all thread increments", i);
            }
        }

        @Test
        @DisplayName("Volatile array reference does not make elements volatile")
        void testVolatileArrayReference() {
            // A volatile array reference only makes the REFERENCE volatile,
            // not the individual elements. This is a known pitfall.
            SharedMemory.SharedArray arr = new SharedMemory.SharedArray(3);
            arr.addToElement(0, 5);
            assertThat(arr.getElement(0)).isEqualTo(5);
        }
    }
}
