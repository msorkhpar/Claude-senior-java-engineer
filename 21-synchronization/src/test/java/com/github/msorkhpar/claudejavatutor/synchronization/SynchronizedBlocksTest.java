package com.github.msorkhpar.claudejavatutor.synchronization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@DisplayName("6.2.1 - Monitors and Synchronized Blocks Tests")
class SynchronizedBlocksTest {

    @Nested
    @DisplayName("Synchronized Counter (method-level)")
    class SynchronizedCounterTests {

        @Test
        @DisplayName("Should increment and decrement correctly in single thread")
        void testSingleThreadIncrementDecrement() {
            var counter = new SynchronizedBlocks.SynchronizedCounter();

            counter.increment();
            counter.increment();
            counter.increment();
            counter.decrement();

            assertThat(counter.getCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should produce correct count under concurrent increments")
        @Timeout(10)
        void testConcurrentIncrements() throws InterruptedException {
            var counter = new SynchronizedBlocks.SynchronizedCounter();
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
            assertThat(counter.getCount()).isEqualTo(numThreads * incrementsPerThread);
        }

        @Test
        @DisplayName("Should handle concurrent increments and decrements correctly")
        @Timeout(10)
        void testConcurrentIncrementsAndDecrements() throws InterruptedException {
            var counter = new SynchronizedBlocks.SynchronizedCounter();
            int numThreads = 10;
            int opsPerThread = 5_000;
            CountDownLatch latch = new CountDownLatch(numThreads * 2);

            // Half threads increment, half decrement
            for (int i = 0; i < numThreads; i++) {
                Thread.ofPlatform().start(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        counter.increment();
                    }
                    latch.countDown();
                });
                Thread.ofPlatform().start(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        counter.decrement();
                    }
                    latch.countDown();
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(counter.getCount()).isEqualTo(0)
                .as("Equal increments and decrements should result in zero");
        }

        @Test
        @DisplayName("Should start at zero")
        void testInitialValue() {
            var counter = new SynchronizedBlocks.SynchronizedCounter();
            assertThat(counter.getCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Block Synchronized Counter (private lock)")
    class BlockSynchronizedCounterTests {

        @Test
        @DisplayName("Should produce correct count under concurrent increments")
        @Timeout(10)
        void testConcurrentIncrements() throws InterruptedException {
            var counter = new SynchronizedBlocks.BlockSynchronizedCounter();
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
            assertThat(counter.getCount()).isEqualTo(numThreads * incrementsPerThread);
        }

        @Test
        @DisplayName("Should handle increment and decrement correctly")
        void testIncrementAndDecrement() {
            var counter = new SynchronizedBlocks.BlockSynchronizedCounter();
            counter.increment();
            counter.increment();
            counter.decrement();
            assertThat(counter.getCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Static vs Instance Locking")
    class StaticVsInstanceLockTests {

        @Test
        @DisplayName("Static and instance locks should be independent")
        @Timeout(10)
        void testStaticAndInstanceLocksAreIndependent() throws InterruptedException {
            SynchronizedBlocks.StaticVsInstanceLock.resetStatic();
            var instance = new SynchronizedBlocks.StaticVsInstanceLock();

            int incrementsPerThread = 5_000;
            CountDownLatch latch = new CountDownLatch(2);

            // Thread incrementing static counter
            Thread.ofPlatform().start(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    SynchronizedBlocks.StaticVsInstanceLock.incrementStatic();
                }
                latch.countDown();
            });

            // Thread incrementing instance counter — different lock, runs concurrently
            Thread.ofPlatform().start(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    instance.incrementInstance();
                }
                latch.countDown();
            });

            latch.await(10, TimeUnit.SECONDS);
            assertThat(SynchronizedBlocks.StaticVsInstanceLock.getStaticCounter())
                .isEqualTo(incrementsPerThread);
            assertThat(instance.getInstanceCounter())
                .isEqualTo(incrementsPerThread);
        }

        @Test
        @DisplayName("Static counter should be shared across instances")
        void testStaticCounterSharedAcrossInstances() {
            SynchronizedBlocks.StaticVsInstanceLock.resetStatic();

            SynchronizedBlocks.StaticVsInstanceLock.incrementStatic();
            SynchronizedBlocks.StaticVsInstanceLock.incrementStatic();

            assertThat(SynchronizedBlocks.StaticVsInstanceLock.getStaticCounter()).isEqualTo(2);
        }

        @Test
        @DisplayName("Instance counter should be independent per instance")
        void testInstanceCounterIndependentPerInstance() {
            var instance1 = new SynchronizedBlocks.StaticVsInstanceLock();
            var instance2 = new SynchronizedBlocks.StaticVsInstanceLock();

            instance1.incrementInstance();
            instance1.incrementInstance();
            instance2.incrementInstance();

            assertThat(instance1.getInstanceCounter()).isEqualTo(2);
            assertThat(instance2.getInstanceCounter()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Reentrancy")
    class ReentrancyTests {

        @Test
        @DisplayName("Should allow reentrant lock acquisition without deadlock")
        void testReentrantCall() {
            var demo = new SynchronizedBlocks.ReentrantDemo();
            demo.outer(); // calls inner() while holding the lock — must not deadlock

            assertThat(demo.getCallLog())
                .containsExactly("outer-start", "inner", "outer-end");
        }

        @Test
        @DisplayName("Should allow reentrant lock acquisition in inheritance chain")
        void testReentrantInheritance() {
            var derived = new SynchronizedBlocks.ReentrantInheritanceDerived();
            derived.doWorkAndLog(); // calls super.doWork() while holding same lock

            assertThat(derived.getLog()).contains("base");
        }

        @Test
        @DisplayName("Reentrant calls should work under concurrent access")
        @Timeout(10)
        void testReentrantUnderConcurrency() throws InterruptedException {
            var demo = new SynchronizedBlocks.ReentrantDemo();
            int numThreads = 10;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                Thread.ofPlatform().start(() -> {
                    demo.outer(); // each thread re-acquires lock for inner()
                    latch.countDown();
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            // Each outer() call adds 3 entries: outer-start, inner, outer-end
            assertThat(demo.getCallLog()).hasSize(numThreads * 3);
        }
    }

    @Nested
    @DisplayName("Lock Release on Exception")
    class LockReleaseOnExceptionTests {

        @Test
        @DisplayName("Should release lock when exception is thrown in synchronized block")
        @Timeout(5)
        void testLockReleasedOnException() throws InterruptedException {
            var demo = new SynchronizedBlocks.LockReleaseOnException();
            AtomicBoolean secondThreadCompleted = new AtomicBoolean(false);

            // First thread: acquire lock and throw exception
            Thread thrower = Thread.ofPlatform().start(() -> {
                try {
                    demo.riskyUpdate(42, true);
                } catch (RuntimeException ignored) {
                    // expected
                }
            });
            thrower.join(2000);

            // Second thread: should be able to acquire the lock
            // (proves the first thread released it despite the exception)
            Thread reader = Thread.ofPlatform().start(() -> {
                int value = demo.getValue();
                secondThreadCompleted.set(true);
            });
            reader.join(2000);

            assertThat(secondThreadCompleted.get()).isTrue()
                .as("Second thread should complete, proving lock was released after exception");
            assertThat(demo.wasExceptionThrown()).isTrue();
            assertThat(demo.getValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should update value even when exception follows")
        void testValueSetBeforeException() {
            var demo = new SynchronizedBlocks.LockReleaseOnException();

            assertThatThrownBy(() -> demo.riskyUpdate(99, true))
                .isInstanceOf(RuntimeException.class);

            assertThat(demo.getValue()).isEqualTo(99)
                .as("Value should be set before exception is thrown");
        }

        @Test
        @DisplayName("Should update value normally when no exception")
        void testNormalUpdate() {
            var demo = new SynchronizedBlocks.LockReleaseOnException();
            demo.riskyUpdate(55, false);

            assertThat(demo.getValue()).isEqualTo(55);
            assertThat(demo.wasExceptionThrown()).isFalse();
        }
    }

    @Nested
    @DisplayName("Fine-Grained Locking")
    class FineGrainedLockingTests {

        @Test
        @DisplayName("Should maintain correct balance under concurrent deposits and withdrawals")
        @Timeout(10)
        void testConcurrentDepositsAndWithdrawals() throws InterruptedException {
            var account = new SynchronizedBlocks.FineGrainedLocking();
            int numThreads = 10;
            int opsPerThread = 1_000;
            int amountPerOp = 10;
            CountDownLatch latch = new CountDownLatch(numThreads * 2);

            // Deposit threads
            for (int i = 0; i < numThreads; i++) {
                Thread.ofPlatform().start(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        account.deposit(amountPerOp);
                    }
                    latch.countDown();
                });
            }

            // Withdraw threads
            for (int i = 0; i < numThreads; i++) {
                Thread.ofPlatform().start(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        account.withdraw(amountPerOp);
                    }
                    latch.countDown();
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            assertThat(account.getBalance()).isEqualTo(0)
                .as("Equal deposits and withdrawals should result in zero balance");
        }

        @Test
        @DisplayName("Should log all transactions")
        void testTransactionLogging() {
            var account = new SynchronizedBlocks.FineGrainedLocking();
            account.deposit(100);
            account.withdraw(50);
            account.deposit(25);

            assertThat(account.getTransactionLog())
                .containsExactly("deposit:100", "withdraw:50", "deposit:25");
            assertThat(account.getBalance()).isEqualTo(75);
        }
    }

    @Nested
    @DisplayName("Wait/Notify (BoundedBuffer)")
    class BoundedBufferTests {

        @Test
        @DisplayName("Should support single-threaded put and take")
        void testSingleThreadPutTake() throws InterruptedException {
            var buffer = new SynchronizedBlocks.BoundedBuffer<Integer>(5);
            buffer.put(1);
            buffer.put(2);

            assertThat(buffer.take()).isEqualTo(1);
            assertThat(buffer.take()).isEqualTo(2);
            assertThat(buffer.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should block producer when buffer is full and unblock when space available")
        @Timeout(5)
        void testProducerBlocksWhenFull() throws InterruptedException {
            var buffer = new SynchronizedBlocks.BoundedBuffer<Integer>(2);
            buffer.put(1);
            buffer.put(2);

            AtomicBoolean producerCompleted = new AtomicBoolean(false);

            // Producer should block because buffer is full
            Thread producer = Thread.ofPlatform().start(() -> {
                try {
                    buffer.put(3); // blocks until consumer takes
                    producerCompleted.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Give producer time to block
            Thread.sleep(100);
            assertThat(producerCompleted.get()).isFalse()
                .as("Producer should be blocked when buffer is full");

            // Consumer takes an item — unblocks producer
            buffer.take();

            await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(producerCompleted.get()).isTrue());
        }

        @Test
        @DisplayName("Should block consumer when buffer is empty and unblock when data available")
        @Timeout(5)
        void testConsumerBlocksWhenEmpty() throws InterruptedException {
            var buffer = new SynchronizedBlocks.BoundedBuffer<String>(5);
            AtomicBoolean consumerCompleted = new AtomicBoolean(false);
            String[] result = new String[1];

            // Consumer should block because buffer is empty
            Thread consumer = Thread.ofPlatform().start(() -> {
                try {
                    result[0] = buffer.take(); // blocks until producer puts
                    consumerCompleted.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread.sleep(100);
            assertThat(consumerCompleted.get()).isFalse()
                .as("Consumer should be blocked when buffer is empty");

            buffer.put("hello");

            await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(consumerCompleted.get()).isTrue();
                    assertThat(result[0]).isEqualTo("hello");
                });
        }

        @Test
        @DisplayName("Should support concurrent producer-consumer pattern")
        @Timeout(10)
        void testConcurrentProducerConsumer() throws InterruptedException {
            var buffer = new SynchronizedBlocks.BoundedBuffer<Integer>(10);
            int itemCount = 1_000;
            CountDownLatch producerDone = new CountDownLatch(1);
            CountDownLatch consumerDone = new CountDownLatch(1);
            int[] sum = {0};

            // Producer
            Thread.ofPlatform().start(() -> {
                try {
                    for (int i = 1; i <= itemCount; i++) {
                        buffer.put(i);
                    }
                    producerDone.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Consumer
            Thread.ofPlatform().start(() -> {
                try {
                    for (int i = 0; i < itemCount; i++) {
                        sum[0] += buffer.take();
                    }
                    consumerDone.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            assertThat(consumerDone.await(10, TimeUnit.SECONDS)).isTrue();
            int expectedSum = itemCount * (itemCount + 1) / 2;
            assertThat(sum[0]).isEqualTo(expectedSum)
                .as("Consumer should receive all items from producer");
        }

        @Test
        @DisplayName("Should handle multiple producers and consumers")
        @Timeout(10)
        void testMultipleProducersAndConsumers() throws InterruptedException {
            var buffer = new SynchronizedBlocks.BoundedBuffer<Integer>(5);
            int numProducers = 3;
            int itemsPerProducer = 500;
            int totalItems = numProducers * itemsPerProducer;

            CountDownLatch producersDone = new CountDownLatch(numProducers);
            CountDownLatch consumersDone = new CountDownLatch(1);
            java.util.concurrent.atomic.AtomicInteger consumed = new java.util.concurrent.atomic.AtomicInteger(0);

            // Producers
            for (int p = 0; p < numProducers; p++) {
                Thread.ofPlatform().start(() -> {
                    try {
                        for (int i = 0; i < itemsPerProducer; i++) {
                            buffer.put(1);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    producersDone.countDown();
                });
            }

            // Single consumer that takes exactly totalItems
            Thread.ofPlatform().start(() -> {
                try {
                    for (int i = 0; i < totalItems; i++) {
                        buffer.take();
                        consumed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                consumersDone.countDown();
            });

            assertThat(producersDone.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(consumersDone.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(consumed.get()).isEqualTo(totalItems);
        }
    }

    @Nested
    @DisplayName("Null Lock Pitfall")
    class NullLockPitfallTests {

        @Test
        @DisplayName("Should throw NullPointerException when synchronizing on null")
        void testSynchronizeOnNull() {
            var demo = new SynchronizedBlocks.NullLockPitfall();

            assertThatThrownBy(() -> demo.synchronizeOn(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should succeed when synchronizing on non-null object")
        void testSynchronizeOnNonNull() {
            var demo = new SynchronizedBlocks.NullLockPitfall();
            // Should not throw
            demo.synchronizeOn(new Object());
        }
    }
}
