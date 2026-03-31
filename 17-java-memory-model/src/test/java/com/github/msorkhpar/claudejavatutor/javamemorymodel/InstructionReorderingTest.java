package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("5.1.4 - Instruction Reordering Tests")
class InstructionReorderingTest {

    @Nested
    @DisplayName("Volatile Stop Flag (Hoisting Prevention)")
    class VolatileStopFlagTests {

        @Test
        @DisplayName("Volatile stop flag must terminate loop when set")
        @Timeout(5)
        void testVolatileStopFlagTerminatesLoop() throws InterruptedException {
            InstructionReordering.VolatileStopFlag demo = new InstructionReordering.VolatileStopFlag();
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch finished = new CountDownLatch(1);

            Thread worker = new Thread(() -> {
                started.countDown();
                demo.spinUntilStopped();
                finished.countDown();
            });

            worker.start();
            started.await(); // wait for worker to begin spinning
            Thread.sleep(50); // let it spin for a bit
            demo.stop();      // volatile write

            // Worker should see the stop signal and exit
            assertThat(finished.await(4, TimeUnit.SECONDS)).isTrue()
                .as("Worker must exit spin loop after volatile stop flag is set");
        }

        @Test
        @DisplayName("Volatile flag should be false initially, true after stop()")
        void testVolatileFlagInitialState() {
            InstructionReordering.VolatileStopFlag demo = new InstructionReordering.VolatileStopFlag();
            assertThat(demo.isStopped()).isFalse()
                .as("Flag should start as false");
            demo.stop();
            assertThat(demo.isStopped()).isTrue()
                .as("Flag should be true after stop()");
        }
    }

    @Nested
    @DisplayName("Volatile as Memory Barrier")
    class VolatileMemoryBarrierTests {

        @Test
        @DisplayName("Volatile write before non-volatile read must maintain ordering")
        void testVolatileWriteBarrier() throws InterruptedException {
            InstructionReordering.VolatileBarrier barrier = new InstructionReordering.VolatileBarrier();
            boolean correct = barrier.runTest();
            assertThat(correct).isTrue()
                .as("Volatile write must flush preceding writes; reader must see data after volatile read");
        }

        @RepeatedTest(5)
        @DisplayName("Repeated volatile barrier test for reliability")
        void testVolatileBarrierRepeated() throws InterruptedException {
            InstructionReordering.VolatileBarrier barrier = new InstructionReordering.VolatileBarrier();
            assertThat(barrier.runTest()).isTrue()
                .as("Volatile barrier must be consistently correct");
        }

        @Test
        @DisplayName("Volatile publication should guarantee constructor completion")
        void testVolatilePublication() throws InterruptedException {
            InstructionReordering.VolatilePublication pub = new InstructionReordering.VolatilePublication();
            boolean result = pub.testPublication();
            assertThat(result).isTrue()
                .as("Volatile publication must ensure object is fully constructed before reference visible");
        }
    }

    @Nested
    @DisplayName("Safe Lazy Initialization (No Reordering Risk)")
    class LazyInitTests {

        @Test
        @DisplayName("IODH singleton avoids reordering without volatile")
        void testIodhSingleton() {
            InstructionReordering.IodhSingleton s1 = InstructionReordering.IodhSingleton.getInstance();
            InstructionReordering.IodhSingleton s2 = InstructionReordering.IodhSingleton.getInstance();

            assertThat(s1).isNotNull();
            assertThat(s1).isSameAs(s2);
            assertThat(s1.isInitialized()).isTrue();
        }

        @Test
        @DisplayName("IODH singleton identity preserved across threads")
        void testIodhSingletonThreadSafety() throws InterruptedException {
            int numThreads = 20;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(numThreads);
            InstructionReordering.IodhSingleton[] instances = new InstructionReordering.IodhSingleton[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int idx = i;
                Thread t = new Thread(() -> {
                    try {
                        start.await();
                        instances[idx] = InstructionReordering.IodhSingleton.getInstance();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
                t.start();
            }

            start.countDown();
            assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();

            InstructionReordering.IodhSingleton expected = instances[0];
            for (InstructionReordering.IodhSingleton inst : instances) {
                assertThat(inst).isSameAs(expected);
            }
        }
    }

    @Nested
    @DisplayName("Final Field Ordering")
    class FinalFieldOrderingTests {

        @Test
        @DisplayName("Final fields provide safe publication guarantee after construction")
        void testFinalFieldVisibility() {
            // Final fields are guaranteed to be fully initialized and visible
            // to any thread that reads them after the constructor completes
            InstructionReordering.FinalFieldObject obj = new InstructionReordering.FinalFieldObject(7, "test");
            assertThat(obj.getX()).isEqualTo(7);
            assertThat(obj.getLabel()).isEqualTo("test");
        }

        @Test
        @DisplayName("Final fields should be visible across threads via volatile publisher")
        void testFinalFieldCrossThread() throws InterruptedException {
            InstructionReordering.FinalFieldPublisher publisher =
                new InstructionReordering.FinalFieldPublisher();
            CountDownLatch done = new CountDownLatch(1);
            final int[] observed = {-1};

            Thread reader = new Thread(() -> {
                InstructionReordering.FinalFieldObject obj;
                while ((obj = publisher.get()) == null) {
                    Thread.onSpinWait();
                }
                observed[0] = obj.getX();
                done.countDown();
            });

            reader.start();
            Thread.sleep(20);
            publisher.publish(new InstructionReordering.FinalFieldObject(55, "data"));

            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(observed[0]).isEqualTo(55)
                .as("Final field value must be visible after safe publication");
        }
    }

    @Nested
    @DisplayName("Synchronized Block Ordering")
    class SynchronizedBlockOrderingTests {

        @Test
        @DisplayName("Operations inside synchronized block cannot be reordered outside it")
        void testSynchronizedOrdering() throws InterruptedException {
            InstructionReordering.SynchronizedOrdering ordering =
                new InstructionReordering.SynchronizedOrdering();
            boolean result = ordering.runConsistencyTest();
            assertThat(result).isTrue()
                .as("Synchronized block must maintain ordering of writes");
        }

        @Test
        @DisplayName("Concurrent synchronized writes and reads should be consistent")
        void testConcurrentSynchronizedConsistency() throws InterruptedException {
            InstructionReordering.SynchronizedOrdering ordering =
                new InstructionReordering.SynchronizedOrdering();
            int numThreads = 5;
            int opsPerThread = 200;
            CountDownLatch latch = new CountDownLatch(numThreads);
            AtomicInteger inconsistencies = new AtomicInteger(0);

            for (int i = 0; i < numThreads; i++) {
                final int tid = i;
                Thread t = new Thread(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        ordering.atomicUpdate(tid * opsPerThread + j, "thread-" + tid);
                        InstructionReordering.SynchronizedOrdering.Pair pair = ordering.atomicRead();
                        // Values should always be consistent (updated together atomically)
                        if (pair.value() < 0) {
                            inconsistencies.incrementAndGet();
                        }
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(15, TimeUnit.SECONDS);
            assertThat(inconsistencies.get()).isZero()
                .as("No inconsistencies should be observed with synchronized access");
        }
    }
}
