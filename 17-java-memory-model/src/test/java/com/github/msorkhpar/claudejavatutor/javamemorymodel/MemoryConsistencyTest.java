package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("5.1.3 - Synchronization and Memory Consistency Tests")
class MemoryConsistencyTest {

    @Nested
    @DisplayName("Synchronized Happens-Before")
    class SynchronizedHappensBeforeTests {

        @Test
        @DisplayName("Synchronized release should make writes visible to the next lock acquisition")
        void testSynchronizedVisibility() throws InterruptedException {
            MemoryConsistency.SynchronizedWriter writer = new MemoryConsistency.SynchronizedWriter();
            CountDownLatch writerDone = new CountDownLatch(1);
            CountDownLatch readerDone = new CountDownLatch(1);
            final int[] observed = {-1, null == null ? -1 : 0}; // [value, nameHash]
            final String[] observedName = {null};

            Thread writerThread = new Thread(() -> {
                writer.write(42, "Alice");
                writerDone.countDown();
            });

            Thread readerThread = new Thread(() -> {
                try {
                    writerDone.await(); // wait for writer to unlock
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                MemoryConsistency.SynchronizedWriter.Values values = writer.read();
                observed[0] = values.value();
                observedName[0] = values.name();
                readerDone.countDown();
            });

            writerThread.start();
            readerThread.start();

            assertThat(readerDone.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(observed[0]).isEqualTo(42)
                .as("Value written under lock must be visible after lock acquired by reader");
            assertThat(observedName[0]).isEqualTo("Alice")
                .as("Name written under lock must be visible after lock acquired by reader");
        }

        @Test
        @DisplayName("Multiple synchronized operations should all be visible in order")
        void testMultipleSynchronizedOperations() throws InterruptedException {
            MemoryConsistency.OrderedCounter counter = new MemoryConsistency.OrderedCounter();
            int numThreads = 5;
            int opsPerThread = 200;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        counter.increment();
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.get()).isEqualTo(numThreads * opsPerThread)
                .as("All increments must be visible — no lost updates");
        }
    }

    @Nested
    @DisplayName("Double-Checked Locking")
    class DoubleCheckedLockingTests {

        @Test
        @DisplayName("DCL singleton should return non-null instance")
        void testDclSingletonNonNull() {
            MemoryConsistency.DCLSingleton instance = MemoryConsistency.DCLSingleton.getInstance();
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("DCL singleton should return same instance on repeated calls")
        void testDclSingletonSameInstance() {
            MemoryConsistency.DCLSingleton i1 = MemoryConsistency.DCLSingleton.getInstance();
            MemoryConsistency.DCLSingleton i2 = MemoryConsistency.DCLSingleton.getInstance();
            assertThat(i1).isSameAs(i2);
        }

        @Test
        @DisplayName("DCL singleton should return same instance across multiple threads")
        void testDclSingletonAcrossThreads() throws InterruptedException {
            int numThreads = 20;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            MemoryConsistency.DCLSingleton[] instances = new MemoryConsistency.DCLSingleton[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int idx = i;
                Thread t = new Thread(() -> {
                    try {
                        startLatch.await();
                        instances[idx] = MemoryConsistency.DCLSingleton.getInstance();
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

            MemoryConsistency.DCLSingleton expected = instances[0];
            for (MemoryConsistency.DCLSingleton inst : instances) {
                assertThat(inst).isSameAs(expected)
                    .as("All threads must get the same DCL singleton instance");
            }
        }

        @Test
        @DisplayName("DCL singleton should be fully initialized")
        void testDclSingletonFullyInitialized() {
            MemoryConsistency.DCLSingleton instance = MemoryConsistency.DCLSingleton.getInstance();
            assertThat(instance.getValue()).isEqualTo(MemoryConsistency.DCLSingleton.EXPECTED_VALUE)
                .as("DCL singleton must be fully initialized before reference is published");
        }
    }

    @Nested
    @DisplayName("Synchronized Write-Read Pairs")
    class SynchronizedWriteReadTests {

        @RepeatedTest(3)
        @DisplayName("Synchronized write followed by synchronized read should be consistent")
        void testSynchronizedWriteRead() throws InterruptedException {
            MemoryConsistency.SynchronizedState state = new MemoryConsistency.SynchronizedState();
            CountDownLatch writeDone = new CountDownLatch(1);

            Thread writer = new Thread(() -> {
                state.update(100, "update100");
                writeDone.countDown();
            });

            writer.start();
            writeDone.await(3, TimeUnit.SECONDS);

            // After writer releases the lock, this thread acquires it
            // and must see the written values
            MemoryConsistency.SynchronizedState.Snapshot snap = state.snapshot();
            assertThat(snap.count()).isEqualTo(100)
                .as("Synchronized read must see latest count");
            assertThat(snap.label()).isEqualTo("update100")
                .as("Synchronized read must see latest label");
        }

        @Test
        @DisplayName("Concurrent updates from multiple threads should all be visible")
        void testConcurrentSynchronizedUpdates() throws InterruptedException {
            MemoryConsistency.ConcurrentAccumulator acc = new MemoryConsistency.ConcurrentAccumulator();
            int numThreads = 10;
            int opsPerThread = 100;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                final int val = i + 1;
                Thread t = new Thread(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        acc.add(val);
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            // Sum = numThreads threads * opsPerThread * (1+2+...+numThreads) / numThreads
            // Each thread adds its val (1..10) opsPerThread times
            // Total = opsPerThread * sum(1..10) = 100 * 55 = 5500
            long expectedTotal = (long) opsPerThread * (numThreads * (numThreads + 1) / 2);
            assertThat(acc.getTotal()).isEqualTo(expectedTotal)
                .as("All additions must be visible in accumulator");
        }
    }

    @Nested
    @DisplayName("Lock Reentrance")
    class ReentranceTests {

        @Test
        @DisplayName("Reentrant synchronized should not deadlock")
        void testReentrantSynchronized() {
            MemoryConsistency.ReentrantExample re = new MemoryConsistency.ReentrantExample();
            // If synchronized were not reentrant, outer() calling inner() would deadlock
            int result = re.outer();
            assertThat(result).isEqualTo(MemoryConsistency.ReentrantExample.EXPECTED_RESULT)
                .as("Reentrant synchronized must not deadlock and produce correct result");
        }

        @Test
        @DisplayName("ReentrantLock should work correctly for mutex behavior")
        void testReentrantLock() throws InterruptedException {
            MemoryConsistency.ReentrantLockCounter counter = new MemoryConsistency.ReentrantLockCounter();
            int numThreads = 10;
            int opsPerThread = 1000;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        counter.increment();
                    }
                    latch.countDown();
                });
                t.start();
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.get()).isEqualTo(numThreads * opsPerThread)
                .as("ReentrantLock-based counter should have no lost updates");
        }
    }

    @Nested
    @DisplayName("Wait/Notify Pattern")
    class WaitNotifyTests {

        @Test
        @DisplayName("wait/notifyAll should coordinate producer and consumer correctly")
        void testWaitNotify() throws InterruptedException {
            MemoryConsistency.ProducerConsumerQueue queue = new MemoryConsistency.ProducerConsumerQueue();
            int numItems = 10;
            CountDownLatch done = new CountDownLatch(numItems);
            AtomicInteger consumed = new AtomicInteger(0);

            Thread consumer = new Thread(() -> {
                while (consumed.get() < numItems) {
                    try {
                        int item = queue.take();
                        consumed.incrementAndGet();
                        done.countDown();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            consumer.start();

            Thread producer = new Thread(() -> {
                for (int i = 0; i < numItems; i++) {
                    try {
                        queue.put(i);
                        Thread.sleep(5); // small delay between produces
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            producer.start();

            assertThat(done.await(15, TimeUnit.SECONDS)).isTrue()
                .as("Consumer should receive all produced items");
            assertThat(consumed.get()).isEqualTo(numItems);
        }
    }
}
