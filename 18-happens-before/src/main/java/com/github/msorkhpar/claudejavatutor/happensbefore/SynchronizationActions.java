package com.github.msorkhpar.claudejavatutor.happensbefore;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates the three core synchronization mechanisms that establish happens-before:
 * <ul>
 *   <li>5.2.2.1 Synchronized methods and blocks — monitor lock rule</li>
 *   <li>5.2.2.2 Volatile variables — volatile variable rule</li>
 *   <li>5.2.2.3 Final fields — safe publication of immutable objects</li>
 * </ul>
 *
 * <p>All tests demonstrate CORRECT behavior. Broken (unsynchronized) versions would be
 * non-deterministic and are described in comments rather than tested.
 *
 * @see README_5.2.2.md
 */
public class SynchronizationActions {

    // ── 5.2.2.1 Synchronized Methods and Blocks ───────────────────────────────

    /**
     * Demonstrates synchronized methods for visibility AND mutual exclusion.
     * Uses the monitor lock rule: unlock hb subsequent lock of same monitor.
     */
    public static class SynchronizedCounter {
        private int count = 0;

        /**
         * Atomic increment protected by 'this' monitor.
         * Unlock hb subsequent lock — all threads see the latest count.
         */
        public synchronized void increment() {
            count++;
        }

        /**
         * Decrement protected by same monitor, demonstrating mutual exclusion.
         */
        public synchronized void decrement() {
            count--;
        }

        /**
         * Synchronized read ensures fresh visibility of count.
         */
        public synchronized int getCount() {
            return count;
        }

        /**
         * Compound check-then-act under single synchronized block.
         * Without the block, the check and increment would be two separate operations.
         */
        public synchronized boolean incrementIfLessThan(int max) {
            if (count < max) {
                count++;
                return true;
            }
            return false;
        }

        /**
         * Reset the counter — useful for testing.
         */
        public synchronized void reset() {
            count = 0;
        }
    }

    /**
     * Demonstrates synchronized blocks (explicit lock object) vs methods.
     * Shows split-lock pattern for fine-grained locking.
     */
    public static class SplitLockDemo {
        private final Object readLock = new Object();
        private final Object writeLock = new Object();
        private volatile int value = 0; // volatile for cross-lock visibility

        /**
         * Write under writeLock.
         */
        public void write(int v) {
            synchronized (writeLock) {
                value = v;
            }
        }

        /**
         * Read under readLock — separate from writeLock to demonstrate
         * that different monitors provide NO mutual exclusion with each other.
         * Here we rely on volatile for visibility across the two different locks.
         */
        public int read() {
            synchronized (readLock) {
                return value; // volatile read inside synchronized — still safe
            }
        }
    }

    /**
     * Demonstrates that synchronized on a class literal protects static state.
     */
    public static class StaticSynchronizedDemo {
        private static int staticCount = 0;

        /**
         * Synchronized static method — uses SynchronizationActions.StaticSynchronizedDemo.class
         * as the monitor (the Class object).
         */
        public static synchronized void incrementStatic() {
            staticCount++;
        }

        public static synchronized int getStaticCount() {
            return staticCount;
        }

        public static synchronized void resetStatic() {
            staticCount = 0;
        }
    }

    // ── 5.2.2.2 Volatile Variables ────────────────────────────────────────────

    /**
     * Demonstrates volatile for a stop-flag pattern (cooperative cancellation).
     * One writer, multiple readers. No compound operation — volatile is exactly right.
     */
    public static class VolatileStopFlag {
        private volatile boolean running = true;
        private final AtomicInteger iterationsCompleted = new AtomicInteger(0);

        /**
         * Signal the worker to stop. Volatile write is immediately visible to the worker.
         */
        public void stop() {
            running = false; // volatile write
        }

        /**
         * Check if the worker should continue running. Volatile read — always fresh.
         */
        public boolean isRunning() {
            return running; // volatile read
        }

        /**
         * Simulates a tight worker loop that checks the volatile flag.
         * In real code this would do actual work; here it counts iterations.
         */
        public void runLoop(int maxIterations) {
            int iterations = 0;
            while (running && iterations < maxIterations) { // volatile read each iteration
                iterations++;
                // Simulate lightweight work
                Thread.onSpinWait();
            }
            iterationsCompleted.set(iterations);
        }

        public int getIterationsCompleted() {
            return iterationsCompleted.get();
        }
    }

    /**
     * Demonstrates volatile for safe publication of an object reference.
     * The volatile write of the reference creates happens-before for all
     * subsequent reads of the reference — but does NOT make the object's fields volatile.
     *
     * <p>To be truly safe, the published object must be immutable or properly synchronized.
     */
    public static class VolatilePublication {
        private volatile ImmutableData published = null; // volatile reference

        /**
         * Safely publishes an immutable object via volatile reference.
         * The volatile write establishes happens-before so readers see the fully
         * initialized immutable object.
         */
        public void publish(String name, int value) {
            published = new ImmutableData(name, value); // volatile write of reference
        }

        /**
         * Safely reads the published object via volatile read.
         * May return null if publish() has not been called yet.
         */
        public ImmutableData get() {
            return published; // volatile read
        }

        /**
         * An immutable data class — all fields final, proper construction.
         * Safe to read without synchronization once safely published.
         */
        public static final class ImmutableData {
            private final String name;
            private final int value;

            public ImmutableData(String name, int value) {
                this.name = name;   // final write in constructor
                this.value = value; // final write in constructor
                // 'this' does NOT escape — final field guarantee applies
            }

            public String getName() { return name; }
            public int getValue() { return value; }
        }
    }

    /**
     * Demonstrates the volatile DCL (Double-Checked Locking) pattern — correct in Java 5+.
     * The volatile on instance prevents the JIT from reordering the object initialization
     * with the store to the instance field.
     */
    public static class VolatileDCL {
        private static volatile VolatileDCL instance; // volatile is ESSENTIAL

        private final int id;

        private VolatileDCL() {
            this.id = 99;
        }

        public static VolatileDCL getInstance() {
            if (instance == null) {                    // first check (no lock, fast path)
                synchronized (VolatileDCL.class) {
                    if (instance == null) {            // second check (under lock)
                        instance = new VolatileDCL(); // volatile write — full memory barrier
                    }
                }
            }
            return instance; // volatile read in fast path
        }

        public int getId() { return id; }
    }

    // ── 5.2.2.3 Final Fields ──────────────────────────────────────────────────

    /**
     * Demonstrates the final field guarantee: writes to final fields in the constructor
     * happen-before any subsequent read of those fields, provided the object is
     * properly constructed (no 'this' escape).
     */
    public static class FinalFieldSafePublication {

        /**
         * Immutable point — all fields final, no mutation after construction.
         * Properly constructed (no 'this' escape).
         * Safe to share across threads once the reference is safely published.
         */
        public static final class ImmutablePoint {
            public final int x;
            public final int y;
            public final String label;

            public ImmutablePoint(int x, int y, String label) {
                this.x = x;           // (W1) final write
                this.y = y;           // (W2) final write
                this.label = label;   // (W3) final write
                // 'this' does NOT escape — guarantee is valid
            }

            public double distanceTo(ImmutablePoint other) {
                int dx = this.x - other.x;
                int dy = this.y - other.y;
                return Math.sqrt(dx * dx + dy * dy);
            }
        }

        /**
         * Demonstrates passing an ImmutablePoint between threads safely.
         * The reference is published via volatile; readers see final fields without locks.
         */
        public static class PointPublisher {
            private volatile ImmutablePoint current = null; // volatile for safe publication

            public void publish(ImmutablePoint p) {
                current = p; // volatile write of reference
            }

            public ImmutablePoint get() {
                return current; // volatile read
            }
        }
    }

    /**
     * Demonstrates proper vs improper construction and the final field guarantee.
     * Shows why 'this' must not escape during construction.
     */
    public static class ProperConstructionDemo {
        public final int safeValue;
        private final CountDownLatch constructionDone;

        /**
         * Properly constructed: sets safeValue before 'this' could be seen externally.
         * The CountDownLatch is notified AFTER the constructor body completes.
         */
        public ProperConstructionDemo(int value, CountDownLatch latch) {
            this.safeValue = value; // final write — guarantee applies
            this.constructionDone = latch;
            // We do NOT call latch.countDown() here — that would let 'this' be seen
            // before final field writes are complete (technically latch is just tracked)
        }

        /**
         * After construction is complete, publish 'this' and notify waiters.
         * This pattern avoids 'this' escape during construction.
         */
        public void publishAndSignal() {
            constructionDone.countDown(); // signal AFTER construction is complete
        }
    }

    // ── VarHandle RELEASE/ACQUIRE (Java 9+) ───────────────────────────────────

    /**
     * Demonstrates VarHandle's RELEASE/ACQUIRE memory ordering modes.
     * This is lighter than full volatile (bidirectional barrier) while still
     * providing the needed happens-before for producer-consumer patterns.
     *
     * <p>RELEASE: all prior stores are visible before this store (producer side).
     * <p>ACQUIRE: this load is visible before all subsequent loads (consumer side).
     */
    public static class VarHandleReleaseAcquire {
        private int data = 0;
        private int published = 0; // accessed via VarHandle for RELEASE/ACQUIRE

        private static final VarHandle PUBLISHED_HANDLE;

        static {
            try {
                PUBLISHED_HANDLE = MethodHandles.lookup()
                        .findVarHandle(VarHandleReleaseAcquire.class, "published", int.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        /**
         * Produce: write data, then publish with RELEASE mode.
         * RELEASE ensures all prior stores (data=value) are visible before the RELEASE store.
         */
        public void produce(int value) {
            data = value;                              // plain write of data
            PUBLISHED_HANDLE.setRelease(this, 1);     // RELEASE: data write hb this store
        }

        /**
         * Consume: read with ACQUIRE mode, then read data.
         * ACQUIRE ensures this load is visible before all subsequent loads.
         * If we see published==1, we are guaranteed to see data==value from produce().
         */
        public int consume() {
            int pub = (int) PUBLISHED_HANDLE.getAcquire(this); // ACQUIRE: hb subsequent reads
            if (pub == 1) {
                return data; // safe: ACQUIRE read of 'published' hb this read of 'data'
            }
            return -1;
        }

        public boolean isPublished() {
            return (int) PUBLISHED_HANDLE.getAcquire(this) == 1;
        }
    }
}
