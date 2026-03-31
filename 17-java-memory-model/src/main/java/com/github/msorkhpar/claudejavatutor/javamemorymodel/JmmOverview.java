package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates core Java Memory Model (JMM) concepts:
 * - Heap vs. stack (thread-local) memory
 * - Happens-before relationships (thread start, join, volatile)
 * - Safe publication patterns
 * - Atomicity basics
 *
 * @see README_5.1.1.md
 */
public class JmmOverview {

    /** A well-known value written before thread start to test happens-before. */
    public static final int WRITTEN_VALUE = 42;

    // -----------------------------------------------------------------------
    // Stack vs. Heap
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that local variables are thread-local.
     * Each thread has its own copy of {@code n} on its stack frame.
     * No synchronization is needed for local variables.
     *
     * @param n the input value (each thread uses its own copy)
     * @return n squared, computed entirely with thread-local variables
     */
    public int computeWithLocalVar(int n) {
        // 'result' and 'n' are on THIS thread's stack — never shared
        int result = n * n;
        return result;
    }

    /**
     * A shared counter stored on the heap (instance field).
     * Multiple threads can access this, so synchronization is required.
     */
    public static class SharedCounter {
        private int count = 0; // heap variable — shared between threads

        /** Thread-safe increment using intrinsic lock. */
        public synchronized void safeIncrement() {
            count++;
        }

        /** Thread-safe read. */
        public synchronized int getValue() {
            return count;
        }
    }

    /**
     * Static fields are shared heap memory — all threads see the same field.
     */
    public static class SharedStaticState {
        public static volatile int volatileValue = 0; // shared, volatile for visibility

        public static void reset() {
            volatileValue = 0;
        }
    }

    // -----------------------------------------------------------------------
    // Happens-Before: thread.start() and thread.join()
    // -----------------------------------------------------------------------

    private int valueForChild = 0;

    /**
     * Writes a value before starting a child thread.
     * thread.start() establishes happens-before with any action in the new thread.
     */
    public void writeBeforeThreadStart() {
        valueForChild = WRITTEN_VALUE; // (1) write before start()
    }

    /**
     * Creates a reader thread that reads the value set by {@link #writeBeforeThreadStart()}.
     * The caller must call start() AFTER calling writeBeforeThreadStart() to establish
     * the happens-before edge.
     */
    public Thread createReaderThread(int[] observed, CountDownLatch done) {
        return new Thread(() -> {
            // Thread.start() happens-before any action here
            // So valueForChild == WRITTEN_VALUE is guaranteed visible
            observed[0] = valueForChild;
            done.countDown();
        });
    }

    /**
     * Demonstrates thread.join() happens-after all actions in the joined thread.
     */
    public static class JoinDemo {
        private int observedValue = 0;
        private int writtenByThread = 0;

        public void runWriterAndJoin() throws InterruptedException {
            Thread writer = new Thread(() -> {
                writtenByThread = WRITTEN_VALUE; // action in thread
            });
            writer.start();
            writer.join(); // join() happens-after all actions in writer
            // After join(), writtenByThread is guaranteed visible
            observedValue = writtenByThread;
        }

        public int getObservedValue() {
            return observedValue;
        }
    }

    // -----------------------------------------------------------------------
    // Happens-Before: volatile
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that a volatile write happens-before a subsequent volatile read
     * of the same variable, making preceding non-volatile writes visible.
     */
    public static class VolatileHappensBefore {
        private int data = 0;
        private volatile boolean ready = false;

        /**
         * Runs writer and reader threads and returns true if the reader
         * observed the correct value of {@code data}.
         */
        public boolean demonstrateHappensBefore() throws InterruptedException {
            CountDownLatch writerDone = new CountDownLatch(1);
            final boolean[] success = {false};

            Thread writer = new Thread(() -> {
                data = WRITTEN_VALUE;  // (1) non-volatile write
                ready = true;           // (2) volatile write — flushes (1)
                writerDone.countDown();
            });

            Thread reader = new Thread(() -> {
                writerDone.countDown(); // signal we started, but wait for writer
                while (!ready) {        // (3) volatile read — acquires (2)'s barrier
                    Thread.onSpinWait();
                }
                // (1) happens-before (2) (program order)
                // (2) happens-before (3) (volatile)
                // therefore data must be WRITTEN_VALUE
                success[0] = (data == WRITTEN_VALUE);
            });

            reader.start();
            writer.start();

            writer.join(3000);
            reader.join(3000);

            return success[0];
        }
    }

    // -----------------------------------------------------------------------
    // Safe Publication
    // -----------------------------------------------------------------------

    /**
     * An immutable point whose coordinates are safely published via final fields.
     * Final field writes in the constructor happen-before any external read,
     * so no synchronization is needed to safely read x and y.
     */
    public static final class ImmutablePoint {
        private final int x;
        private final int y;

        public ImmutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public int getY() { return y; }
    }

    /**
     * Publishes an {@link ImmutablePoint} safely via a volatile reference.
     * The volatile write of the reference establishes happens-before for all threads
     * that subsequently read the reference.
     */
    public static class VolatilePublisher {
        private volatile ImmutablePoint point; // volatile ensures safe publication

        public void publish(ImmutablePoint p) {
            this.point = p; // volatile write
        }

        public ImmutablePoint get() {
            return point; // volatile read
        }
    }

    /**
     * Demonstrates the Initialization-on-Demand Holder pattern for lazy, thread-safe
     * singleton initialization without volatile or explicit synchronization.
     *
     * <p>The JVM guarantees that class initialization (static initializers) is thread-safe.
     * The Holder class is loaded only on the first call to {@link #getInstance()}, providing
     * lazy initialization. Once loaded, {@code INSTANCE} is safely published via the class
     * loading happens-before guarantee.
     */
    public static class LazySingleton {
        private final int value;

        private LazySingleton() {
            this.value = 99; // expensive initialization would go here
        }

        private static class Holder {
            // Class initialization lock ensures thread safety — loaded lazily
            static final LazySingleton INSTANCE = new LazySingleton();
        }

        public static LazySingleton getInstance() {
            return Holder.INSTANCE; // Holder class initialized lazily on first access
        }

        public int getValue() { return value; }
    }
}
