package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates data races and techniques to prevent them.
 * <p>
 * A data race occurs when two or more threads access the same shared variable concurrently,
 * at least one access is a write, and there is no happens-before relationship ordering the accesses.
 * <p>
 * Patterns covered:
 * <ul>
 *   <li>Unsynchronized counter (demonstrating the data race)</li>
 *   <li>Synchronized counter (fix via mutual exclusion)</li>
 *   <li>Atomic counter (fix via CAS-based operations)</li>
 *   <li>Check-then-act race and its fix with ConcurrentHashMap</li>
 *   <li>Immutable objects for race-free sharing</li>
 *   <li>ThreadLocal for thread-confined state</li>
 * </ul>
 *
 * @see README_5.3.1.md
 */
public class DataRace {

    // ── Pitfall 1: Unsynchronized Counter ────────────────────────────────────

    /**
     * An UNSAFE counter with a data race on the counter field.
     * Multiple threads calling increment() concurrently will lose updates
     * because counter++ is a non-atomic read-modify-write operation.
     */
    public static class UnsynchronizedCounter {
        private int counter = 0;

        public void increment() {
            counter++; // NOT atomic: read, increment, write
        }

        public int getCounter() {
            return counter;
        }
    }

    // ── Fix 1a: Synchronized Counter ─────────────────────────────────────────

    /**
     * A thread-safe counter using synchronized methods.
     * The monitor lock on 'this' provides both mutual exclusion and memory visibility.
     */
    public static class SynchronizedCounter {
        private int counter = 0;

        public synchronized void increment() {
            counter++;
        }

        public synchronized int getCounter() {
            return counter;
        }

        public synchronized void reset() {
            counter = 0;
        }
    }

    // ── Fix 1b: Atomic Counter ───────────────────────────────────────────────

    /**
     * A thread-safe counter using AtomicInteger.
     * CAS-based operations provide atomicity without locking.
     */
    public static class AtomicCounter {
        private final AtomicInteger counter = new AtomicInteger(0);

        public void increment() {
            counter.incrementAndGet();
        }

        public int getCounter() {
            return counter.get();
        }

        public void reset() {
            counter.set(0);
        }
    }

    // ── Pitfall 2: Check-Then-Act Race ───────────────────────────────────────

    /**
     * Demonstrates a check-then-act race condition on a HashMap.
     * Two threads can both see a key as absent and both insert, leading to lost updates.
     */
    public static class UnsafeCheckThenAct {
        private final Map<String, String> cache = new HashMap<>();

        /**
         * UNSAFE: check (containsKey) and act (put) are not atomic together.
         */
        public String getOrCompute(String key, java.util.function.Function<String, String> compute) {
            if (!cache.containsKey(key)) {
                cache.put(key, compute.apply(key));
            }
            return cache.get(key);
        }

        public int size() {
            return cache.size();
        }
    }

    /**
     * Thread-safe check-then-act using ConcurrentHashMap.computeIfAbsent.
     */
    public static class SafeCheckThenAct {
        private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
        private final AtomicInteger computeCount = new AtomicInteger(0);

        /**
         * Thread-safe: computeIfAbsent guarantees the mapping function is called at most once per key.
         */
        public String getOrCompute(String key, java.util.function.Function<String, String> compute) {
            return cache.computeIfAbsent(key, k -> {
                computeCount.incrementAndGet();
                return compute.apply(k);
            });
        }

        public int size() {
            return cache.size();
        }

        public int getComputeCount() {
            return computeCount.get();
        }
    }

    // ── Fix 3: Immutable Objects ─────────────────────────────────────────────

    /**
     * An immutable point that can be safely shared across threads without synchronization.
     * All fields are final; no mutation after construction.
     */
    public record ImmutablePoint(int x, int y) {
        /**
         * Returns a new ImmutablePoint translated by (dx, dy).
         * The original point is unchanged -- immutable.
         */
        public ImmutablePoint translate(int dx, int dy) {
            return new ImmutablePoint(x + dx, y + dy);
        }

        /**
         * Distance from origin.
         */
        public double distanceFromOrigin() {
            return Math.sqrt((double) x * x + (double) y * y);
        }
    }

    /**
     * A holder that safely publishes immutable points via a volatile reference.
     * Writers replace the point atomically; readers always see a fully constructed point.
     */
    public static class SafePointHolder {
        private volatile ImmutablePoint point;

        public SafePointHolder(ImmutablePoint initial) {
            this.point = Objects.requireNonNull(initial);
        }

        public void setPoint(ImmutablePoint p) {
            this.point = Objects.requireNonNull(p);
        }

        public ImmutablePoint getPoint() {
            return point;
        }
    }

    // ── Fix 4: ThreadLocal for Per-Thread State ──────────────────────────────

    /**
     * Demonstrates ThreadLocal to eliminate shared mutable state entirely.
     * Each thread maintains its own independent counter.
     */
    public static class ThreadLocalCounter {
        private static final ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> 0);

        public static void increment() {
            counter.set(counter.get() + 1);
        }

        public static int get() {
            return counter.get();
        }

        public static void reset() {
            counter.remove();
        }
    }

    // ── Utility: Run concurrent increments ───────────────────────────────────

    /**
     * Runs the given task concurrently across the specified number of threads,
     * each executing incrementsPerThread iterations.
     * Returns when all threads have completed.
     */
    public static void runConcurrentIncrements(int threadCount, int incrementsPerThread, Runnable task)
            throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    start.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        task.run();
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
    }
}
