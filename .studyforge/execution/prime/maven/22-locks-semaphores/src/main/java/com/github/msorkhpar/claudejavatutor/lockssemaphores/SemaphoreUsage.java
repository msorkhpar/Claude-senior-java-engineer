package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates Semaphore usage (6.3.2).
 * <p>
 * Topics covered:
 * - Semaphore class and its methods: acquire(), release(), tryAcquire() (6.3.2.1)
 * - Controlling access to shared resources via a resource pool (6.3.2.2)
 * - Bounded and unbounded semaphores (6.3.2.3)
 * - Binary semaphore vs mutex
 * - Start gate pattern (Semaphore(0))
 * - Fair vs non-fair semaphores
 * - drainPermits()
 */
public class SemaphoreUsage {

    // -----------------------------------------------------------------------
    // Resource Pool using counting semaphore (6.3.2.2)
    // -----------------------------------------------------------------------

    /**
     * A simple resource pool that limits concurrent access to a fixed number
     * of resources. Demonstrates the primary use case for counting semaphores.
     *
     * @param <R> the resource type
     */
    public static class ResourcePool<R> {
        private final Semaphore semaphore;
        private final Queue<R> resources;

        public ResourcePool(Queue<R> initialResources, boolean fair) {
            this.resources = new ConcurrentLinkedQueue<>(initialResources);
            this.semaphore = new Semaphore(initialResources.size(), fair);
        }

        /**
         * Acquires a resource, blocking until one is available.
         *
         * @return the acquired resource
         * @throws InterruptedException if interrupted while waiting
         */
        public R acquire() throws InterruptedException {
            semaphore.acquire();
            return resources.poll();
        }

        /**
         * Attempts to acquire a resource without blocking.
         *
         * @return the resource, or null if none available
         */
        public R tryAcquire() {
            if (semaphore.tryAcquire()) {
                return resources.poll();
            }
            return null;
        }

        /**
         * Attempts to acquire a resource within the specified timeout.
         *
         * @param timeout maximum time to wait
         * @param unit    time unit
         * @return the resource, or null if timeout expired
         * @throws InterruptedException if interrupted while waiting
         */
        public R tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
            if (semaphore.tryAcquire(timeout, unit)) {
                return resources.poll();
            }
            return null;
        }

        /**
         * Returns a resource to the pool.
         *
         * @param resource the resource to return
         */
        public void release(R resource) {
            resources.offer(resource);
            semaphore.release();
        }

        /**
         * Returns the number of available resources (permits).
         */
        public int availableCount() {
            return semaphore.availablePermits();
        }

        /**
         * Returns true if there are threads waiting for resources.
         */
        public boolean hasWaiters() {
            return semaphore.hasQueuedThreads();
        }

        /**
         * Returns the estimated number of threads waiting.
         */
        public int getWaiterCount() {
            return semaphore.getQueueLength();
        }
    }

    // -----------------------------------------------------------------------
    // Binary semaphore for mutual exclusion and signaling
    // -----------------------------------------------------------------------

    /**
     * Demonstrates binary semaphore usage (1 permit).
     * Unlike ReentrantLock, any thread can release the permit.
     */
    public static class BinarySemaphoreCounter {
        private final Semaphore semaphore = new Semaphore(1);
        private long count;

        public void increment() throws InterruptedException {
            semaphore.acquire();
            try {
                count++;
            } finally {
                semaphore.release();
            }
        }

        public long getCount() throws InterruptedException {
            semaphore.acquire();
            try {
                return count;
            } finally {
                semaphore.release();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Start gate pattern using Semaphore(0)
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the start gate pattern where threads wait until a coordinator
     * releases permits. Uses Semaphore(0) so all threads block on acquire()
     * until release(N) is called.
     *
     * @param threadCount number of threads to coordinate
     * @param task        the task each thread will run after the gate opens
     * @return a Semaphore that threads release when they complete
     * @throws InterruptedException if interrupted
     */
    public static Semaphore startGate(int threadCount, Runnable task) throws InterruptedException {
        Semaphore gate = new Semaphore(0);
        Semaphore done = new Semaphore(0);

        for (int i = 0; i < threadCount; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    gate.acquire(); // block until gate opens
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.release(); // signal completion
                }
            });
        }

        // Open the gate for all threads at once
        gate.release(threadCount);
        return done;
    }

    // -----------------------------------------------------------------------
    // Semaphore utility methods demonstration
    // -----------------------------------------------------------------------

    /**
     * Demonstrates drainPermits() — atomically acquires all available permits.
     *
     * @param semaphore the semaphore to drain
     * @return the number of permits drained
     */
    public static int drainAllPermits(Semaphore semaphore) {
        return semaphore.drainPermits();
    }

    /**
     * Demonstrates that release() can be called without prior acquire(),
     * increasing the permit count beyond the initial value.
     * This is the "unbounded semaphore" behavior.
     *
     * @param initialPermits initial permit count
     * @param extraReleases  number of extra release() calls
     * @return the resulting available permits
     */
    public static int demonstratePermitInflation(int initialPermits, int extraReleases) {
        Semaphore sem = new Semaphore(initialPermits);
        for (int i = 0; i < extraReleases; i++) {
            sem.release();
        }
        return sem.availablePermits();
    }

    /**
     * Acquires multiple permits atomically using acquire(n).
     *
     * @param semaphore the semaphore
     * @param permits   number of permits to acquire
     * @throws InterruptedException if interrupted while waiting
     */
    public static void acquireMultiple(Semaphore semaphore, int permits) throws InterruptedException {
        semaphore.acquire(permits);
    }

    /**
     * Releases multiple permits at once using release(n).
     *
     * @param semaphore the semaphore
     * @param permits   number of permits to release
     */
    public static void releaseMultiple(Semaphore semaphore, int permits) {
        semaphore.release(permits);
    }
}
