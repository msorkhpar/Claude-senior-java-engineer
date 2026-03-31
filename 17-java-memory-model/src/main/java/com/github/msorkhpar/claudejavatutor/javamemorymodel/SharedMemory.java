package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates the distinction between shared heap memory and thread-local stack memory
 * in the Java Memory Model:
 * <ul>
 *   <li>Volatile fields for cross-thread visibility</li>
 *   <li>ThreadLocal for per-thread state</li>
 *   <li>Safe publication patterns</li>
 *   <li>Volatile long/double atomicity</li>
 *   <li>Array element sharing pitfalls</li>
 * </ul>
 *
 * @see README_5.1.2.md
 */
public class SharedMemory {

    // -----------------------------------------------------------------------
    // Volatile Flag — demonstrates visibility of a boolean stop flag
    // -----------------------------------------------------------------------

    /**
     * A thread-safe stop flag using volatile.
     * Without volatile, a busy-waiting thread may cache the value of the flag
     * in a register and never see the update from another thread.
     */
    public static class VolatileFlag {
        private volatile boolean stop = false; // volatile ensures visibility

        /** Called by the controlling thread to signal stop. */
        public void stop() { this.stop = true; }

        /** Reset for reuse (test utility). */
        public void reset() { this.stop = false; }

        /** Called by the worker thread — must see the latest value. */
        public boolean shouldStop() { return stop; }
    }

    // -----------------------------------------------------------------------
    // Volatile Piggybacking — demonstrates how volatile carries other writes
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that a volatile write "piggybacks" all preceding writes:
     * when Thread B reads the volatile flag as true, it is guaranteed to also
     * see all writes made by Thread A before the volatile write.
     */
    public static class VolatilePiggybacking {
        private int nonVolatileData = 0;
        private volatile boolean ready = false;

        /**
         * Runs writer (sets data, then volatile flag) and reader (waits on flag, reads data).
         *
         * @return true if reader observed the correct value of nonVolatileData
         */
        public boolean run() throws InterruptedException {
            CountDownLatch done = new CountDownLatch(1);
            final boolean[] success = {false};

            Thread writer = new Thread(() -> {
                nonVolatileData = 99;   // (1) non-volatile write
                ready = true;            // (2) volatile write — happens-after (1) by program order
                                         // (2) is a memory fence for (1)
            });

            Thread reader = new Thread(() -> {
                while (!ready) {         // (3) volatile read
                    Thread.onSpinWait(); // spin until writer sets ready
                }
                // (1) hb (2) hb (3), so nonVolatileData must be 99
                success[0] = (nonVolatileData == 99);
                done.countDown();
            });

            reader.start();
            writer.start();

            writer.join(3000);
            done.await(java.util.concurrent.TimeUnit.SECONDS.toMillis(3),
                java.util.concurrent.TimeUnit.MILLISECONDS);

            return success[0];
        }
    }

    // -----------------------------------------------------------------------
    // ThreadLocal — per-thread state
    // -----------------------------------------------------------------------

    /**
     * Demonstrates ThreadLocal usage for per-thread state management.
     * Each thread gets its own independent copy of the thread ID — no sharing occurs.
     */
    public static class ThreadLocalDemo {
        // Each thread starts with -1 (meaning "not set")
        private final ThreadLocal<Integer> threadId =
            ThreadLocal.withInitial(() -> -1);

        /** Set this thread's ID. */
        public void setId(int id) {
            threadId.set(id);
        }

        /**
         * Get this thread's ID.
         * Returns -1 if not set (initial value).
         */
        public int getId() {
            return threadId.get();
        }

        /**
         * CRITICAL in thread pools: remove the value to prevent memory leaks
         * and stale state being seen by the next task that runs on this thread.
         */
        public void cleanup() {
            threadId.remove();
        }
    }

    // -----------------------------------------------------------------------
    // Volatile long — 64-bit atomicity
    // -----------------------------------------------------------------------

    /**
     * Demonstrates volatile long for safe 64-bit value sharing.
     *
     * <p>Non-volatile long/double may be split into two 32-bit operations on some JVMs.
     * Volatile long is guaranteed to be atomically read/written per the JMM spec.
     */
    public static class VolatileLongDemo {
        private volatile long counter = 0L; // volatile ensures 64-bit atomic access
        private final Object lock = new Object();

        /**
         * Synchronized increment — volatile provides visibility, synchronized provides atomicity
         * of the compound read-modify-write operation.
         */
        public void increment() {
            synchronized (lock) {
                counter++; // read-modify-write: needs synchronized for correctness
            }
        }

        public long getCount() {
            synchronized (lock) {
                return counter;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Safe Publication via volatile
    // -----------------------------------------------------------------------

    /**
     * Demonstrates safe publication of an object via a volatile reference.
     * The volatile write ensures that the fully initialized Config object
     * is visible to threads that subsequently read the volatile field.
     */
    public static class SafePublisher {
        private volatile Config config; // volatile reference for safe publication

        public void publish(Config c) {
            this.config = c; // volatile write — all preceding writes visible to readers
        }

        public Config getConfig() {
            return config; // volatile read — sees latest write
        }

        /**
         * An immutable configuration object. Final fields provide additional
         * safe-publication guarantee.
         */
        public static final class Config {
            private final String host;
            private final int port;

            public Config(String host, int port) {
                this.host = host;
                this.port = port;
            }

            public String getHost() { return host; }
            public int getPort() { return port; }
        }
    }

    // -----------------------------------------------------------------------
    // Shared array — pitfall: volatile array != volatile elements
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that a volatile array reference does NOT make the elements volatile.
     * Uses explicit synchronization to protect array element access.
     *
     * <p>Key insight: {@code volatile int[] arr} means the array reference is volatile
     * (other threads see reassignments of arr immediately), but reads and writes to
     * {@code arr[i]} are NOT atomic or volatile.
     */
    public static class SharedArray {
        private final int[] elements; // elements guarded by 'this'
        private final Object lock = new Object();

        public SharedArray(int size) {
            this.elements = new int[size];
        }

        /** Atomically adds delta to elements[index]. */
        public void addToElement(int index, int delta) {
            synchronized (lock) {
                elements[index] += delta;
            }
        }

        /** Returns the current value of elements[index]. */
        public int getElement(int index) {
            synchronized (lock) {
                return elements[index];
            }
        }
    }
}
