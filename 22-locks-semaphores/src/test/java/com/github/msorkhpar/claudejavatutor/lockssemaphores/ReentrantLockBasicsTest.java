package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ReentrantLockBasics}.
 * Covers: lock/unlock, tryLock, lockInterruptibly, reentrancy, fairness, diagnostics.
 */
class ReentrantLockBasicsTest {

    private ReentrantLockBasics counter;

    @BeforeEach
    void setUp() {
        counter = new ReentrantLockBasics();
    }

    // -----------------------------------------------------------------------
    // Basic lock/unlock
    // -----------------------------------------------------------------------

    @Test
    void testSingleThreadIncrement() {
        counter.increment();
        counter.increment();
        counter.increment();
        assertThat(counter.getCount()).isEqualTo(3);
    }

    @Test
    void testInitialCountIsZero() {
        assertThat(counter.getCount()).isZero();
    }

    // -----------------------------------------------------------------------
    // tryLock
    // -----------------------------------------------------------------------

    @Test
    void testTryIncrementWhenLockAvailable() {
        boolean result = counter.tryIncrement();
        assertThat(result).isTrue();
        assertThat(counter.getCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Timed tryLock
    // -----------------------------------------------------------------------

    @Test
    void testTimedIncrementSuccess() throws InterruptedException {
        boolean result = counter.timedIncrement(100, TimeUnit.MILLISECONDS);
        assertThat(result).isTrue();
        assertThat(counter.getCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // lockInterruptibly
    // -----------------------------------------------------------------------

    @Test
    void testInterruptibleIncrementSuccess() throws InterruptedException {
        counter.interruptibleIncrement();
        assertThat(counter.getCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Reentrancy
    // -----------------------------------------------------------------------

    @Test
    void testReentrancyHoldCount() {
        int holdCount = counter.demonstrateReentrancy();
        assertThat(holdCount).isEqualTo(2);
        assertThat(counter.getHoldCount()).isZero();
    }

    @Test
    void testReentrancyDoesNotDeadlock() {
        for (int i = 0; i < 100; i++) {
            assertThat(counter.demonstrateReentrancy()).isEqualTo(2);
        }
    }

    // -----------------------------------------------------------------------
    // Fairness
    // -----------------------------------------------------------------------

    @Test
    void testNonFairByDefault() {
        assertThat(counter.isFair()).isFalse();
    }

    @Test
    void testFairLock() {
        ReentrantLockBasics fairCounter = new ReentrantLockBasics(true);
        assertThat(fairCounter.isFair()).isTrue();
    }

    // -----------------------------------------------------------------------
    // Diagnostics
    // -----------------------------------------------------------------------

    @Test
    void testIsLockedAndIsHeldByCurrentThread() {
        assertThat(counter.isLocked()).isFalse();
        assertThat(counter.isHeldByCurrentThread()).isFalse();

        ReentrantLock lock = counter.getLock();
        lock.lock();
        try {
            assertThat(counter.isLocked()).isTrue();
            assertThat(counter.isHeldByCurrentThread()).isTrue();
        } finally {
            lock.unlock();
        }
        assertThat(counter.isLocked()).isFalse();
    }

    @Test
    void testUnlockWithoutLockThrowsException() {
        assertThatThrownBy(() -> counter.getLock().unlock())
                .isInstanceOf(IllegalMonitorStateException.class);
    }

    @Test
    void testHoldCountZeroWhenNotLocked() {
        assertThat(counter.getHoldCount()).isZero();
    }

    @Test
    void testHasQueuedThreadsWhenNoContention() {
        assertThat(counter.hasQueuedThreads()).isFalse();
        assertThat(counter.getQueueLength()).isZero();
    }

    // -----------------------------------------------------------------------
    // Concurrent tests
    // -----------------------------------------------------------------------

    @Test
    @Timeout(10)
    void testConcurrentIncrements() throws InterruptedException {
        int threadCount = 4;
        int incrementsPerThread = 500;
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            counter.increment();
                        }
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }
        assertThat(counter.getCount()).isEqualTo((long) threadCount * incrementsPerThread);
    }

    @Test
    @Timeout(10)
    void testFairLockConcurrency() throws InterruptedException {
        ReentrantLockBasics fairCounter = new ReentrantLockBasics(true);
        int threadCount = 3;
        int incrementsPerThread = 200;
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            fairCounter.increment();
                        }
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }
        assertThat(fairCounter.getCount()).isEqualTo((long) threadCount * incrementsPerThread);
    }

    @Test
    @Timeout(10)
    void testTryIncrementFailsWhenLockHeld() throws InterruptedException {
        ReentrantLock lock = counter.getLock();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                acquired.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } finally {
                lock.unlock();
            }
        });
        holder.setDaemon(true);
        holder.start();
        assertThat(acquired.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(counter.tryIncrement()).isFalse();

        release.countDown();
        holder.join(5000);
    }

    @Test
    @Timeout(10)
    void testTimedIncrementTimeout() throws InterruptedException {
        ReentrantLock lock = counter.getLock();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                acquired.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } finally {
                lock.unlock();
            }
        });
        holder.setDaemon(true);
        holder.start();
        assertThat(acquired.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(counter.timedIncrement(50, TimeUnit.MILLISECONDS)).isFalse();

        release.countDown();
        holder.join(5000);
    }

    @Test
    @Timeout(10)
    void testInterruptibleIncrementInterrupted() throws InterruptedException {
        ReentrantLock lock = counter.getLock();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                acquired.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } finally {
                lock.unlock();
            }
        });
        holder.setDaemon(true);
        holder.start();
        assertThat(acquired.await(5, TimeUnit.SECONDS)).isTrue();

        AtomicReference<Exception> caught = new AtomicReference<>();
        CountDownLatch ready = new CountDownLatch(1);
        Thread waiter = new Thread(() -> {
            ready.countDown();
            try {
                counter.interruptibleIncrement();
            } catch (InterruptedException e) {
                caught.set(e);
            }
        });
        waiter.setDaemon(true);
        waiter.start();
        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(100);

        waiter.interrupt();
        waiter.join(5000);
        assertThat(caught.get()).isInstanceOf(InterruptedException.class);

        release.countDown();
        holder.join(5000);
    }

    @Test
    @Timeout(10)
    void testQueuedThreadsDiagnostics() throws InterruptedException {
        ReentrantLock lock = counter.getLock();
        lock.lock();
        try {
            CountDownLatch started = new CountDownLatch(1);
            Thread waiter = new Thread(() -> {
                started.countDown();
                lock.lock();
                try { /* acquired */ } finally { lock.unlock(); }
            });
            waiter.setDaemon(true);
            waiter.start();
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(200);

            assertThat(counter.hasQueuedThreads()).isTrue();
            assertThat(counter.getQueueLength()).isGreaterThanOrEqualTo(1);
        } finally {
            lock.unlock();
        }
        Thread.sleep(100);
    }
}
