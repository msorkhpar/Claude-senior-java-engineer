package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Tests for {@link SemaphoreUsage}.
 * Covers: ResourcePool, BinarySemaphoreCounter, start gate pattern,
 * drainPermits, permit inflation, multi-permit acquire/release.
 */
@Timeout(30)
class SemaphoreUsageTest {

    // -----------------------------------------------------------------------
    // ResourcePool
    // -----------------------------------------------------------------------

    @Test
    void testResourcePoolAcquireAndRelease() throws InterruptedException {
        Queue<String> resources = new LinkedList<>();
        resources.add("R1");
        resources.add("R2");
        resources.add("R3");

        var pool = new SemaphoreUsage.ResourcePool<>(resources, false);
        assertThat(pool.availableCount()).isEqualTo(3);

        String r1 = pool.acquire();
        assertThat(r1).isNotNull();
        assertThat(pool.availableCount()).isEqualTo(2);

        String r2 = pool.acquire();
        assertThat(r2).isNotNull();
        assertThat(pool.availableCount()).isEqualTo(1);

        pool.release(r1);
        assertThat(pool.availableCount()).isEqualTo(2);

        pool.release(r2);
        assertThat(pool.availableCount()).isEqualTo(3);
    }

    @Test
    void testResourcePoolTryAcquireWhenAvailable() {
        Queue<String> resources = new LinkedList<>();
        resources.add("R1");

        var pool = new SemaphoreUsage.ResourcePool<>(resources, false);
        String result = pool.tryAcquire();
        assertThat(result).isEqualTo("R1");
        assertThat(pool.availableCount()).isZero();
    }

    @Test
    void testResourcePoolTryAcquireWhenEmpty() throws InterruptedException {
        Queue<String> resources = new LinkedList<>();
        resources.add("R1");

        var pool = new SemaphoreUsage.ResourcePool<>(resources, false);
        pool.acquire(); // take the only resource

        String result = pool.tryAcquire();
        assertThat(result).isNull();
    }

    @Test
    void testResourcePoolTimedTryAcquireTimeout() throws InterruptedException {
        Queue<String> resources = new LinkedList<>();
        resources.add("R1");

        var pool = new SemaphoreUsage.ResourcePool<>(resources, false);
        pool.acquire(); // take the only resource

        String result = pool.tryAcquire(100, TimeUnit.MILLISECONDS);
        assertThat(result).isNull();
    }

    @Test
    void testResourcePoolTimedTryAcquireSuccess() throws InterruptedException {
        Queue<String> resources = new LinkedList<>();
        resources.add("R1");

        var pool = new SemaphoreUsage.ResourcePool<>(resources, false);
        String result = pool.tryAcquire(100, TimeUnit.MILLISECONDS);
        assertThat(result).isEqualTo("R1");
    }

    @Test
    void testResourcePoolBlocksWhenExhausted() throws InterruptedException {
        Queue<String> resources = new LinkedList<>();
        resources.add("R1");

        var pool = new SemaphoreUsage.ResourcePool<>(resources, false);
        pool.acquire(); // exhaust the pool

        AtomicBoolean acquired = new AtomicBoolean(false);
        Thread waiter = Thread.ofVirtual().start(() -> {
            try {
                pool.acquire();
                acquired.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread.sleep(200);
        assertThat(acquired.get()).isFalse();
        assertThat(pool.hasWaiters()).isTrue();
        assertThat(pool.getWaiterCount()).isGreaterThanOrEqualTo(1);

        // Release to unblock
        pool.release("R1");
        await().atMost(5, TimeUnit.SECONDS).untilTrue(acquired);

        waiter.join(5000);
    }

    @Test
    void testResourcePoolConcurrentAccess() throws InterruptedException {
        int poolSize = 3;
        Queue<String> resources = new LinkedList<>();
        for (int i = 0; i < poolSize; i++) {
            resources.add("R" + i);
        }

        var pool = new SemaphoreUsage.ResourcePool<>(resources, true);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger currentConcurrent = new AtomicInteger(0);
        int threadCount = 10;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    start.await();
                    String resource = pool.acquire();
                    int c = currentConcurrent.incrementAndGet();
                    maxConcurrent.updateAndGet(max -> Math.max(max, c));
                    Thread.sleep(50);
                    currentConcurrent.decrementAndGet();
                    pool.release(resource);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(30, TimeUnit.SECONDS);

        // At most poolSize threads should have been concurrent
        assertThat(maxConcurrent.get()).isLessThanOrEqualTo(poolSize);
    }

    // -----------------------------------------------------------------------
    // BinarySemaphoreCounter
    // -----------------------------------------------------------------------

    @Test
    void testBinarySemaphoreCounterSingleThread() throws InterruptedException {
        var counter = new SemaphoreUsage.BinarySemaphoreCounter();
        counter.increment();
        counter.increment();
        counter.increment();
        assertThat(counter.getCount()).isEqualTo(3);
    }

    @Test
    void testBinarySemaphoreCounterConcurrent() throws InterruptedException {
        var counter = new SemaphoreUsage.BinarySemaphoreCounter();
        int threadCount = 10;
        int incrementsPerThread = 500;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    start.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(15, TimeUnit.SECONDS);

        assertThat(counter.getCount()).isEqualTo((long) threadCount * incrementsPerThread);
    }

    // -----------------------------------------------------------------------
    // Start gate pattern
    // -----------------------------------------------------------------------

    @Test
    void testStartGateAllThreadsRun() throws InterruptedException {
        AtomicInteger executionCount = new AtomicInteger(0);
        int threadCount = 5;

        Semaphore doneSemaphore = SemaphoreUsage.startGate(threadCount, executionCount::incrementAndGet);

        // Wait for all threads to complete
        doneSemaphore.acquire(threadCount);

        assertThat(executionCount.get()).isEqualTo(threadCount);
    }

    @Test
    void testStartGateSingleThread() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean(false);

        Semaphore doneSemaphore = SemaphoreUsage.startGate(1, () -> ran.set(true));
        doneSemaphore.acquire(1);

        assertThat(ran.get()).isTrue();
    }

    // -----------------------------------------------------------------------
    // drainPermits
    // -----------------------------------------------------------------------

    @Test
    void testDrainAllPermits() {
        Semaphore sem = new Semaphore(5);
        int drained = SemaphoreUsage.drainAllPermits(sem);
        assertThat(drained).isEqualTo(5);
        assertThat(sem.availablePermits()).isZero();
    }

    @Test
    void testDrainPermitsWhenNoneAvailable() {
        Semaphore sem = new Semaphore(0);
        int drained = SemaphoreUsage.drainAllPermits(sem);
        assertThat(drained).isZero();
    }

    // -----------------------------------------------------------------------
    // Permit inflation (unbounded behavior)
    // -----------------------------------------------------------------------

    @Test
    void testPermitInflation() {
        int result = SemaphoreUsage.demonstratePermitInflation(3, 5);
        assertThat(result).isEqualTo(8); // 3 initial + 5 extra releases
    }

    @Test
    void testPermitInflationWithZeroInitial() {
        int result = SemaphoreUsage.demonstratePermitInflation(0, 3);
        assertThat(result).isEqualTo(3);
    }

    @Test
    void testNoInflation() {
        int result = SemaphoreUsage.demonstratePermitInflation(5, 0);
        assertThat(result).isEqualTo(5);
    }

    // -----------------------------------------------------------------------
    // Multi-permit acquire and release
    // -----------------------------------------------------------------------

    @Test
    void testAcquireMultiplePermits() throws InterruptedException {
        Semaphore sem = new Semaphore(10);
        SemaphoreUsage.acquireMultiple(sem, 4);
        assertThat(sem.availablePermits()).isEqualTo(6);
    }

    @Test
    void testReleaseMultiplePermits() {
        Semaphore sem = new Semaphore(2);
        SemaphoreUsage.releaseMultiple(sem, 3);
        assertThat(sem.availablePermits()).isEqualTo(5);
    }

    @Test
    void testAcquireAndReleaseMultiple() throws InterruptedException {
        Semaphore sem = new Semaphore(10);
        SemaphoreUsage.acquireMultiple(sem, 7);
        assertThat(sem.availablePermits()).isEqualTo(3);

        SemaphoreUsage.releaseMultiple(sem, 7);
        assertThat(sem.availablePermits()).isEqualTo(10);
    }

    // -----------------------------------------------------------------------
    // Semaphore core API tests
    // -----------------------------------------------------------------------

    @Test
    void testSemaphoreFairness() {
        Semaphore fair = new Semaphore(1, true);
        Semaphore nonFair = new Semaphore(1, false);

        assertThat(fair.isFair()).isTrue();
        assertThat(nonFair.isFair()).isFalse();
    }

    @Test
    void testSemaphoreReleaseBeforeAcquire() throws InterruptedException {
        Semaphore sem = new Semaphore(0);
        assertThat(sem.availablePermits()).isZero();

        sem.release(); // release before acquire is legal
        assertThat(sem.availablePermits()).isEqualTo(1);

        sem.acquire(); // succeeds immediately
        assertThat(sem.availablePermits()).isZero();
    }

    @Test
    void testTryAcquireNonBlocking() {
        Semaphore sem = new Semaphore(1);
        assertThat(sem.tryAcquire()).isTrue();
        assertThat(sem.tryAcquire()).isFalse(); // no permits left
        sem.release();
        assertThat(sem.tryAcquire()).isTrue(); // available again
    }

    @Test
    void testAcquireUninterruptibly() {
        Semaphore sem = new Semaphore(1);
        sem.acquireUninterruptibly();
        assertThat(sem.availablePermits()).isZero();
        sem.release();
    }
}
