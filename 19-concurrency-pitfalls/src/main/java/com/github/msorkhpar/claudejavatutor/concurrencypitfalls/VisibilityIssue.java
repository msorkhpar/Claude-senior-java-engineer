package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Demonstrates visibility issues and proper synchronization techniques.
 * <p>
 * Visibility issues occur when one thread writes a value but another thread cannot see the update
 * because there is no happens-before relationship between the write and the read. The JIT compiler
 * and CPU caches can cause threads to work with stale values indefinitely.
 * <p>
 * Patterns covered:
 * <ul>
 *   <li>Non-volatile flag (visibility problem: thread may loop forever)</li>
 *   <li>Volatile flag (fix: ensures visibility across threads)</li>
 *   <li>Piggybacking visibility on synchronized (flag + data consistency)</li>
 *   <li>Double-checked locking (broken without volatile vs correct with volatile)</li>
 *   <li>Safe publication of objects via volatile</li>
 * </ul>
 *
 * @see README_5.3.3.md
 */
public class VisibilityIssue {

    // ── Pitfall 1: Non-Volatile Flag ─────────────────────────────────────────

    /**
     * UNSAFE: The stop flag is not volatile. The JIT may hoist the read out of the loop,
     * causing the worker thread to loop forever even after another thread sets stop = true.
     * <p>
     * In practice this is hard to reproduce reliably in tests (the JIT may or may not
     * optimize aggressively), so we demonstrate the fix alongside it.
     */
    public static class NonVolatileFlag {
        private boolean stop = false;  // NOT volatile -- visibility issue!

        public void requestStop() {
            stop = true;
        }

        public boolean isStopRequested() {
            return stop;
        }

        /**
         * Spins until stop is true. Without volatile, JIT may hoist the read.
         * Returns the number of iterations performed.
         */
        public long spinUntilStop(long maxIterations) {
            long iterations = 0;
            while (!stop && iterations < maxIterations) {
                iterations++;
            }
            return iterations;
        }
    }

    // ── Fix 1: Volatile Flag ─────────────────────────────────────────────────

    /**
     * Thread-safe stop flag using volatile.
     * Volatile ensures that every read of 'stop' fetches the latest value from main memory.
     */
    public static class VolatileFlag {
        private volatile boolean stop = false;

        public void requestStop() {
            stop = true;
        }

        public boolean isStopRequested() {
            return stop;
        }

        /**
         * Spins until stop is true. With volatile, the JIT cannot hoist the read.
         */
        public long spinUntilStop(long maxIterations) {
            long iterations = 0;
            while (!stop && iterations < maxIterations) {
                iterations++;
            }
            return iterations;
        }
    }

    // ── Pitfall 2: Volatile Does NOT Provide Atomicity for Compound Ops ──────

    /**
     * Demonstrates that volatile alone does NOT make i++ atomic.
     * Even though the write is immediately visible, the read-modify-write
     * is still three separate steps.
     */
    public static class VolatileCounter {
        private volatile int count = 0;

        /**
         * NOT thread-safe despite volatile!
         * volatile ensures visibility but not atomicity of the compound operation.
         */
        public void increment() {
            count++;  // read + increment + write -- 3 non-atomic steps
        }

        public int getCount() {
            return count;
        }
    }

    // ── Fix 3: Piggybacking Visibility on Synchronized ───────────────────────

    /**
     * Demonstrates that synchronized provides BOTH mutual exclusion AND memory visibility.
     * When a thread exits a synchronized block, all writes made within that block become
     * visible to any thread that subsequently enters a synchronized block on the same monitor.
     */
    public static class SynchronizedVisibility {
        private int data = 0;
        private boolean ready = false;

        /**
         * Writer: sets data, then sets ready to true.
         * Both writes are visible to any thread that reads under the same monitor.
         */
        public synchronized void publish(int value) {
            data = value;
            ready = true;
        }

        /**
         * Reader: reads ready and data under the same monitor.
         * If ready is true, data is guaranteed to have the published value.
         */
        public synchronized int readIfReady() {
            if (ready) {
                return data;
            }
            return -1;
        }

        public synchronized boolean isReady() {
            return ready;
        }
    }

    // ── Pitfall 4: Broken Double-Checked Locking (without volatile) ──────────

    /**
     * Demonstrates CORRECT double-checked locking with volatile.
     * Without volatile, the reference could be seen as non-null by another thread
     * before the object is fully constructed (due to instruction reordering).
     */
    public static class DoubleCheckedLocking<T> {
        private volatile T instance;  // MUST be volatile for correctness
        private final java.util.function.Supplier<T> factory;

        public DoubleCheckedLocking(java.util.function.Supplier<T> factory) {
            this.factory = Objects.requireNonNull(factory);
        }

        /**
         * Lazily initializes and returns the singleton instance.
         * The double-checked locking pattern avoids synchronization on the fast path.
         */
        public T getInstance() {
            T result = instance;  // volatile read
            if (result == null) {
                synchronized (this) {
                    result = instance;
                    if (result == null) {
                        result = factory.get();
                        instance = result;  // volatile write
                    }
                }
            }
            return result;
        }

        /**
         * For testing: whether the instance has been initialized.
         */
        public boolean isInitialized() {
            return instance != null;
        }
    }

    // ── Fix 5: Safe Publication via Volatile ─────────────────────────────────

    /**
     * Demonstrates safe publication: a writer sets data fields and then publishes
     * the "ready" signal via a volatile write. The reader polls the volatile flag,
     * and once it sees true, all prior writes are guaranteed visible (piggybacking
     * on the volatile happens-before).
     */
    public static class VolatilePublisher {
        private int value1;
        private int value2;
        private String label;
        private volatile boolean published = false;

        /**
         * Publish data. The volatile write to 'published' ensures that value1, value2,
         * and label are all visible to readers who see published == true.
         */
        public void publish(int v1, int v2, String lbl) {
            value1 = v1;
            value2 = v2;
            label = lbl;
            published = true;  // volatile write -- establishes happens-before
        }

        public boolean isPublished() {
            return published;
        }

        /**
         * Read published data. Only valid after isPublished() returns true.
         */
        public int getValue1() {
            return value1;
        }

        public int getValue2() {
            return value2;
        }

        public String getLabel() {
            return label;
        }
    }

    // ── Holder-Based Lazy Initialization (JLS-guaranteed thread safety) ──────

    /**
     * The Initialization-on-Demand Holder idiom.
     * The JLS guarantees that a class is initialized at most once, and the initialization
     * happens-before any use of that class. This provides thread-safe lazy initialization
     * without volatile or synchronized.
     */
    public static class HolderIdiom {
        private HolderIdiom() {}

        private static class Holder {
            static final HolderIdiom INSTANCE = new HolderIdiom();
        }

        public static HolderIdiom getInstance() {
            return Holder.INSTANCE;
        }
    }
}
