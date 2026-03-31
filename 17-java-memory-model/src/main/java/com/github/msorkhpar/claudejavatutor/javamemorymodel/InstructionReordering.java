package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates instruction reordering and how Java's synchronization primitives
 * (volatile, synchronized) create memory barriers to prevent problematic reorderings:
 * <ul>
 *   <li>Volatile stop flag prevents JIT hoisting of loop condition</li>
 *   <li>Volatile write barrier ensures preceding writes are visible after volatile read</li>
 *   <li>Final fields prevent reordering of constructor writes</li>
 *   <li>Synchronized blocks prevent any reordering across block boundaries</li>
 *   <li>IODH singleton — no reordering risk due to class loading guarantee</li>
 * </ul>
 *
 * @see README_5.1.4.md
 */
public class InstructionReordering {

    // -----------------------------------------------------------------------
    // Volatile stop flag — prevents JIT hoisting of loop condition
    // -----------------------------------------------------------------------

    /**
     * Demonstrates how volatile prevents the JIT from hoisting the loop condition.
     *
     * <p>Without volatile, the JIT may transform:
     * <pre>
     *     while (!stopped) { Thread.onSpinWait(); }
     * </pre>
     * into:
     * <pre>
     *     if (!stopped) { while (true) { Thread.onSpinWait(); } }
     * </pre>
     * because from a single-threaded perspective, {@code stopped} never changes
     * inside the loop. With volatile, every loop iteration reads from main memory.
     */
    public static class VolatileStopFlag {
        private volatile boolean stopped = false; // volatile — prevents hoisting

        /** Called by a controlling thread to request stop. */
        public void stop() {
            stopped = true; // volatile write
        }

        /** Worker loop — must observe the stop signal via volatile read. */
        public void spinUntilStopped() {
            while (!stopped) { // volatile read on every iteration
                Thread.onSpinWait(); // hint to CPU that this is a spin-wait
            }
        }

        public boolean isStopped() { return stopped; }
    }

    // -----------------------------------------------------------------------
    // Volatile as memory barrier — write before, read after
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the memory barrier semantics of volatile:
     * a volatile write acts as a "release" (all preceding writes are visible to anyone
     * who reads the volatile field), and a volatile read acts as an "acquire"
     * (all writes made before the volatile write are visible after the read).
     */
    public static class VolatileBarrier {
        private int data = 0;
        private volatile boolean written = false; // volatile for barrier semantics

        /**
         * Runs a writer (sets data, then volatile flag) and a reader
         * (waits on volatile flag, then reads data).
         *
         * @return true if reader observed the correct value of data
         */
        public boolean runTest() throws InterruptedException {
            // Reset for reuse
            data = 0;
            written = false;

            CountDownLatch done = new CountDownLatch(1);
            final boolean[] success = {false};

            Thread writer = new Thread(() -> {
                data = 100;      // (1) non-volatile write
                written = true;  // (2) volatile write — StoreStore barrier before this
                                 // data=100 cannot be reordered after this point
            });

            Thread reader = new Thread(() -> {
                while (!written) {  // (3) volatile read — LoadLoad barrier after this
                    Thread.onSpinWait();
                }
                // (1) hb (2) hb (3) → data must be 100
                success[0] = (data == 100);
                done.countDown();
            });

            reader.start();
            writer.start();

            boolean await = done.await(5, TimeUnit.SECONDS);
            writer.join(1000);
            reader.join(1000);

            return await && success[0];
        }
    }

    // -----------------------------------------------------------------------
    // Volatile publication — object reference publication
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that publishing an object reference via a volatile field ensures
     * that the object's fields are fully initialized before the reference is visible.
     *
     * <p>Without volatile on the reference, the JIT may reorder the constructor writes
     * to come AFTER the reference store, so another thread could see the reference
     * pointing to a partially constructed object.
     */
    public static class VolatilePublication {
        private volatile PublishedObject obj; // volatile prevents reordering

        public boolean testPublication() throws InterruptedException {
            obj = null;
            CountDownLatch done = new CountDownLatch(1);
            final boolean[] success = {false};

            Thread reader = new Thread(() -> {
                PublishedObject local;
                while ((local = obj) == null) { // volatile read
                    Thread.onSpinWait();
                }
                // If obj is non-null, it must be fully constructed
                success[0] = local.isFullyInitialized();
                done.countDown();
            });

            reader.start();
            Thread.sleep(10);
            obj = new PublishedObject(42, "hello"); // volatile write — full barrier

            done.await(5, TimeUnit.SECONDS);
            reader.join(1000);

            return success[0];
        }

        /**
         * An object with meaningful initialization — tests that construction is
         * complete before the reference is observed.
         */
        public static class PublishedObject {
            private final int value;
            private final String label;

            public PublishedObject(int value, String label) {
                this.value = value;
                this.label = label;
                // Both fields set in constructor before reference escapes
            }

            public boolean isFullyInitialized() {
                return value == 42 && "hello".equals(label);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Initialization-on-Demand Holder — no reordering risk
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the IODH idiom: class initialization is guaranteed thread-safe by
     * the JVM's class loading lock. No volatile needed. The Holder class is only
     * loaded (and INSTANCE initialized) on the first call to {@link #getInstance()}.
     *
     * <p>This avoids the reordering risk of DCL by relying on the well-specified
     * class initialization happens-before guarantee.
     */
    public static class IodhSingleton {
        private final boolean initialized;

        private IodhSingleton() {
            this.initialized = true; // set in constructor
        }

        private static class Holder {
            // Class loading is thread-safe — JVM class init lock guarantees ordering
            static final IodhSingleton INSTANCE = new IodhSingleton();
        }

        public static IodhSingleton getInstance() {
            return Holder.INSTANCE; // lazily loaded on first access
        }

        public boolean isInitialized() { return initialized; }
    }

    // -----------------------------------------------------------------------
    // Final fields — construction barrier
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that final fields provide a safe publication guarantee:
     * writes to final fields in a constructor happen-before any external
     * read of those fields, provided the reference is not leaked before
     * the constructor completes.
     */
    public static final class FinalFieldObject {
        private final int x;       // final — construction barrier guaranteed
        private final String label; // final — construction barrier guaranteed

        public FinalFieldObject(int x, String label) {
            this.x = x;
            this.label = label;
        }

        public int getX() { return x; }
        public String getLabel() { return label; }
    }

    /**
     * Publishes a {@link FinalFieldObject} via a volatile reference.
     * Combining final fields (for object construction ordering) with volatile
     * publication (for reference visibility) provides maximum safety.
     */
    public static class FinalFieldPublisher {
        private volatile FinalFieldObject published; // volatile for reference visibility

        public void publish(FinalFieldObject obj) {
            this.published = obj; // volatile write
        }

        public FinalFieldObject get() {
            return published; // volatile read
        }
    }

    // -----------------------------------------------------------------------
    // Synchronized block — prevents all reorderings across block boundaries
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that operations inside a synchronized block cannot be reordered
     * to appear outside the block. This provides the strongest ordering guarantee
     * short of sequential consistency.
     */
    public static class SynchronizedOrdering {
        private int value = 0;
        private String label = "";
        private final Object lock = new Object();

        /**
         * Atomically updates value and label under the lock.
         * No reordering can move these writes outside the synchronized block.
         */
        public void atomicUpdate(int v, String l) {
            synchronized (lock) {
                value = v;  // cannot reorder before monitor entry
                label = l;  // cannot reorder after monitor exit
            }               // StoreStore + StoreLoad barrier on exit
        }

        /**
         * Atomically reads value and label under the same lock.
         * Guaranteed to see the writes from atomicUpdate() if that method
         * has released the lock before this call acquires it.
         */
        public Pair atomicRead() {
            synchronized (lock) {  // LoadLoad + LoadStore barrier on entry
                return new Pair(value, label);
            }
        }

        /**
         * Runs a consistency test: writer sets data, then volatile signals;
         * reader waits for signal, then checks consistency under the lock.
         */
        public boolean runConsistencyTest() throws InterruptedException {
            // Reset
            synchronized (lock) {
                value = 0;
                label = "initial";
            }

            CountDownLatch done = new CountDownLatch(1);
            final boolean[] success = {false};

            Thread writer = new Thread(() -> {
                atomicUpdate(999, "written");
            });

            Thread reader = new Thread(() -> {
                writer_done: {
                    try {
                        writer.join(3000); // thread join establishes happens-before
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Pair pair = atomicRead();
                    success[0] = (pair.value() == 999 && "written".equals(pair.label()));
                }
                done.countDown();
            });

            writer.start();
            reader.start();

            done.await(5, TimeUnit.SECONDS);
            writer.join(1000);
            reader.join(1000);

            return success[0];
        }

        public record Pair(int value, String label) {}
    }
}
