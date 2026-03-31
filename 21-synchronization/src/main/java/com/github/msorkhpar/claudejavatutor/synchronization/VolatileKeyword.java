package com.github.msorkhpar.claudejavatutor.synchronization;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates the volatile keyword in Java:
 * <ul>
 *   <li>Memory visibility guarantees</li>
 *   <li>Volatile flag pattern for thread termination</li>
 *   <li>Happens-before relationships via volatile</li>
 *   <li>Atomicity limitations of volatile (volatile does NOT make compound ops atomic)</li>
 *   <li>Double-checked locking with volatile</li>
 *   <li>Volatile vs synchronized vs AtomicInteger comparison</li>
 * </ul>
 *
 * @see README_6.2.2.md
 */
public class VolatileKeyword {

    // -----------------------------------------------------------------------
    // Volatile Flag — the canonical volatile use case
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the volatile flag pattern for cooperative thread stopping.
     * Without volatile, the worker thread might cache the value of {@code running}
     * in a CPU register and never see the update from the controlling thread.
     */
    public static class VolatileFlag {
        private volatile boolean running = true;
        private int workCount = 0;

        /** Signal the worker to stop. Volatile write is immediately visible. */
        public void stop() {
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

        /**
         * Simulates a worker loop that checks the volatile flag on each iteration.
         * The loop will terminate when another thread calls {@link #stop()}.
         */
        public void doWork() {
            while (running) {
                workCount++;
                // In real code this would do actual work
                Thread.onSpinWait();
            }
        }

        public int getWorkCount() {
            return workCount;
        }
    }

    // -----------------------------------------------------------------------
    // Non-Volatile Flag — demonstrates visibility bug
    // -----------------------------------------------------------------------

    /**
     * Demonstrates what can go wrong WITHOUT volatile.
     * The JVM may optimize away reads to the non-volatile field, causing the
     * worker thread to spin forever even after {@code stop()} is called.
     *
     * <p>Note: This bug is non-deterministic. It may or may not manifest
     * depending on JIT compilation, CPU architecture, and runtime conditions.
     * It's most likely to appear with server JIT (-server) and long-running loops.
     */
    public static class NonVolatileFlag {
        private boolean running = true; // NOT volatile — visibility not guaranteed
        private int workCount = 0;

        public void stop() {
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

        /**
         * May run indefinitely because the JVM is allowed to cache {@code running}
         * and never re-read it from main memory.
         */
        public void doWork() {
            while (running) {
                workCount++;
                Thread.onSpinWait();
            }
        }

        public int getWorkCount() {
            return workCount;
        }
    }

    // -----------------------------------------------------------------------
    // Happens-Before via Volatile — piggybacking non-volatile writes
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the happens-before guarantee of volatile:
     * all writes before a volatile write are visible to any thread
     * that subsequently reads the same volatile variable.
     *
     * <p>This means you can safely publish non-volatile state through
     * a volatile "ready" flag.
     */
    public static class VolatileHappensBefore {
        private int data = 0;           // NOT volatile
        private String message = null;  // NOT volatile
        private volatile boolean ready = false; // volatile flag

        /**
         * Sets non-volatile state then writes volatile flag.
         * JMM guarantees: data and message writes happen-before ready=true.
         */
        public void publish(int data, String message) {
            this.data = data;
            this.message = message;
            this.ready = true; // volatile write — memory fence
        }

        /**
         * Reads the volatile flag first, then reads non-volatile state.
         * If ready is true, data and message are guaranteed to be visible.
         *
         * @return true if the published data was correctly observed
         */
        public boolean consume() {
            if (ready) { // volatile read — happens-after the volatile write
                return data != 0 && message != null;
            }
            return false;
        }

        public int getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }

        public boolean isReady() {
            return ready;
        }
    }

    // -----------------------------------------------------------------------
    // Volatile Atomicity Pitfall — compound operations are NOT atomic
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that volatile does NOT provide atomicity for compound operations.
     * {@code count++} is a read-modify-write sequence: read count, add 1, write count.
     * Even with volatile, two threads can read the same value and both write
     * count+1, losing an increment (race condition).
     */
    public static class VolatileCounter {
        private volatile int count = 0;

        /**
         * NOT thread-safe despite volatile! The increment is a compound operation.
         */
        public void increment() {
            count++; // read-modify-write: NOT atomic
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * Thread-safe counter using AtomicInteger, which provides
     * hardware-level CAS (Compare-And-Swap) for atomic compound operations.
     */
    public static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        /** Atomically increments using CAS — always thread-safe. */
        public void increment() {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }
    }

    /**
     * Thread-safe counter using synchronized — provides both atomicity
     * and mutual exclusion for compound operations.
     */
    public static class SynchronizedCounter {
        private int count = 0;

        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    // -----------------------------------------------------------------------
    // Double-Checked Locking — requires volatile for correctness
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the correct double-checked locking (DCL) pattern.
     * The volatile keyword is REQUIRED because without it, the JVM may
     * reorder the initialization of the object and the assignment to the
     * reference, causing another thread to see a partially constructed object.
     *
     * @param <T> the type of the lazily initialized value
     */
    public static class DoubleCheckedLazy<T> {
        private volatile T instance; // MUST be volatile for DCL correctness
        private final java.util.function.Supplier<T> supplier;

        public DoubleCheckedLazy(java.util.function.Supplier<T> supplier) {
            this.supplier = supplier;
        }

        /**
         * Returns the lazily initialized instance using double-checked locking.
         * <ol>
         *   <li>First check without lock — fast path if already initialized</li>
         *   <li>Acquire lock and check again — ensures only one thread initializes</li>
         *   <li>Volatile write ensures full construction is visible to all threads</li>
         * </ol>
         */
        public T getInstance() {
            T local = instance; // volatile read — may avoid second volatile read below
            if (local == null) {
                synchronized (this) {
                    local = instance;
                    if (local == null) {
                        local = supplier.get();
                        instance = local; // volatile write
                    }
                }
            }
            return local;
        }

        /** Returns true if the instance has been initialized. */
        public boolean isInitialized() {
            return instance != null;
        }
    }

    // -----------------------------------------------------------------------
    // Volatile 64-bit Atomicity — long and double
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that volatile guarantees atomic reads/writes for 64-bit types.
     * Without volatile, reads and writes to {@code long} and {@code double}
     * may be non-atomic on 32-bit JVMs (word tearing: two separate 32-bit operations).
     */
    public static class Volatile64Bit {
        private volatile long volatileTimestamp = 0L;
        private long nonVolatileTimestamp = 0L;

        public void setVolatileTimestamp(long value) {
            volatileTimestamp = value; // guaranteed atomic even on 32-bit JVMs
        }

        public long getVolatileTimestamp() {
            return volatileTimestamp; // guaranteed atomic read
        }

        public void setNonVolatileTimestamp(long value) {
            nonVolatileTimestamp = value; // may NOT be atomic on 32-bit JVMs
        }

        public long getNonVolatileTimestamp() {
            return nonVolatileTimestamp;
        }
    }
}
