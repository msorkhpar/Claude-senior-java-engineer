package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("5.1.1 - JMM Overview Tests")
class JmmOverviewTest {

    @Nested
    @DisplayName("Heap vs Stack Memory Distinction")
    class HeapVsStackTests {

        @Test
        @DisplayName("Should demonstrate that local variables are thread-local")
        void testLocalVariablesAreThreadLocal() throws InterruptedException {
            JmmOverview overview = new JmmOverview();
            int numThreads = 5;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            AtomicInteger errors = new AtomicInteger(0);

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                Thread t = new Thread(() -> {
                    try {
                        startLatch.await();
                        // Each thread computes with its own local variables
                        int result = overview.computeWithLocalVar(threadId);
                        if (result != threadId * threadId) {
                            errors.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
                t.start();
            }

            startLatch.countDown();
            assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(errors.get()).isZero()
                .as("Local variables should never interfere across threads");
        }

        @Test
        @DisplayName("Should demonstrate heap object sharing between threads")
        void testHeapObjectSharedBetweenThreads() throws InterruptedException {
            JmmOverview.SharedCounter counter = new JmmOverview.SharedCounter();
            int numThreads = 10;
            int incrementsPerThread = 100;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.safeIncrement(); // uses synchronized — correct
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.getValue()).isEqualTo(numThreads * incrementsPerThread)
                .as("Synchronized increments on shared heap object must be consistent");
        }

        @Test
        @DisplayName("Should demonstrate that static fields are shared heap memory")
        void testStaticFieldsAreShared() throws InterruptedException {
            JmmOverview.SharedStaticState.reset();
            CountDownLatch latch = new CountDownLatch(2);

            Thread writer = new Thread(() -> {
                JmmOverview.SharedStaticState.volatileValue = 42;
                latch.countDown();
            });

            final int[] observed = {0};
            Thread reader = new Thread(() -> {
                latch.countDown();
                // Wait for writer to complete
                while (JmmOverview.SharedStaticState.volatileValue == 0) {
                    Thread.onSpinWait();
                }
                observed[0] = JmmOverview.SharedStaticState.volatileValue;
            });

            reader.start();
            writer.start();
            writer.join(3000);
            reader.join(3000);

            assertThat(observed[0]).isEqualTo(42)
                .as("Volatile static field visible across threads");
        }
    }

    @Nested
    @DisplayName("Happens-Before Relationships")
    class HappensBeforeTests {

        @Test
        @DisplayName("Should demonstrate thread start happens-before actions in started thread")
        void testThreadStartHappensBefore() throws InterruptedException {
            JmmOverview overview = new JmmOverview();
            overview.writeBeforeThreadStart();

            final int[] observed = new int[1];
            CountDownLatch done = new CountDownLatch(1);

            // Thread.start() happens-before any action in the new thread
            Thread t = overview.createReaderThread(observed, done);
            t.start(); // start() establishes happens-before

            done.await(5, TimeUnit.SECONDS);
            assertThat(observed[0]).isEqualTo(JmmOverview.WRITTEN_VALUE)
                .as("Thread.start() happens-before actions in the started thread");
        }

        @Test
        @DisplayName("Should demonstrate thread join happens-after all actions in joined thread")
        void testThreadJoinHappensAfter() throws InterruptedException {
            JmmOverview.JoinDemo demo = new JmmOverview.JoinDemo();
            demo.runWriterAndJoin();

            assertThat(demo.getObservedValue()).isEqualTo(JmmOverview.WRITTEN_VALUE)
                .as("All actions in thread happen-before Thread.join() returns");
        }

        @Test
        @DisplayName("Should demonstrate volatile write happens-before volatile read")
        void testVolatileHappensBefore() throws InterruptedException {
            JmmOverview.VolatileHappensBefore demo = new JmmOverview.VolatileHappensBefore();
            boolean result = demo.demonstrateHappensBefore();

            assertThat(result).isTrue()
                .as("Volatile write happens-before volatile read of same variable");
        }
    }

    @Nested
    @DisplayName("Safe Publication")
    class SafePublicationTests {

        @Test
        @DisplayName("Should safely publish immutable object via final field")
        void testSafePublicationViaFinalField() {
            JmmOverview.ImmutablePoint point = new JmmOverview.ImmutablePoint(3, 4);

            assertThat(point.getX()).isEqualTo(3);
            assertThat(point.getY()).isEqualTo(4);
            // Final fields are safely published — no synchronization needed to read them
        }

        @Test
        @DisplayName("Should safely publish via volatile reference")
        void testSafePublicationViaVolatile() throws InterruptedException {
            JmmOverview.VolatilePublisher publisher = new JmmOverview.VolatilePublisher();
            CountDownLatch done = new CountDownLatch(1);
            final int[] observedX = {-1};

            Thread reader = new Thread(() -> {
                JmmOverview.ImmutablePoint p;
                while ((p = publisher.get()) == null) {
                    Thread.onSpinWait();
                }
                observedX[0] = p.getX();
                done.countDown();
            });

            reader.start();
            Thread.sleep(10); // Give reader time to spin
            publisher.publish(new JmmOverview.ImmutablePoint(7, 8));

            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(observedX[0]).isEqualTo(7)
                .as("Volatile publication ensures fully constructed object is visible");
        }

        @Test
        @DisplayName("Should demonstrate initialization-on-demand holder singleton")
        void testInitializationOnDemandHolder() {
            JmmOverview.LazySingleton s1 = JmmOverview.LazySingleton.getInstance();
            JmmOverview.LazySingleton s2 = JmmOverview.LazySingleton.getInstance();

            assertThat(s1).isNotNull();
            assertThat(s1).isSameAs(s2)
                .as("IODH singleton must return same instance");
        }

        @Test
        @DisplayName("Should demonstrate singleton identity across multiple threads")
        void testSingletonIdentityAcrossThreads() throws InterruptedException {
            int numThreads = 20;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            JmmOverview.LazySingleton[] instances = new JmmOverview.LazySingleton[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int idx = i;
                Thread t = new Thread(() -> {
                    try {
                        startLatch.await();
                        instances[idx] = JmmOverview.LazySingleton.getInstance();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
                t.start();
            }

            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();

            JmmOverview.LazySingleton expected = instances[0];
            for (JmmOverview.LazySingleton instance : instances) {
                assertThat(instance).isSameAs(expected)
                    .as("All threads must get the same singleton instance");
            }
        }
    }

    @Nested
    @DisplayName("Atomicity Concepts")
    class AtomicityTests {

        @Test
        @DisplayName("Should demonstrate non-atomic compound read-modify-write with AtomicInteger workaround")
        void testAtomicIncrement() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);
            int threads = 10;
            int ops = 1000;
            CountDownLatch latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < ops; j++) {
                        counter.incrementAndGet(); // atomic CAS
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.get()).isEqualTo(threads * ops)
                .as("AtomicInteger.incrementAndGet() is atomic — no lost updates");
        }

        @Test
        @DisplayName("Should demonstrate synchronized increment is correct")
        void testSynchronizedIncrementIsCorrect() throws InterruptedException {
            JmmOverview.SharedCounter counter = new JmmOverview.SharedCounter();
            int threads = 10;
            int ops = 1000;
            CountDownLatch latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < ops; j++) {
                        counter.safeIncrement();
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.getValue()).isEqualTo(threads * ops)
                .as("Synchronized increment must have no lost updates");
        }

        @Test
        @DisplayName("Should return correct value from computeWithLocalVar")
        void testComputeWithLocalVar() {
            JmmOverview overview = new JmmOverview();
            assertThat(overview.computeWithLocalVar(5)).isEqualTo(25);
            assertThat(overview.computeWithLocalVar(0)).isEqualTo(0);
            assertThat(overview.computeWithLocalVar(-3)).isEqualTo(9);
        }
    }
}
