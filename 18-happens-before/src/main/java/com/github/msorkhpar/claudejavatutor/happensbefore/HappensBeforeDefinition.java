package com.github.msorkhpar.claudejavatutor.happensbefore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates the six core happens-before rules defined by the Java Memory Model (JLS §17.4.5).
 * <p>
 * Rules covered:
 * 1. Program Order Rule — actions within a thread are ordered.
 * 2. Monitor Lock Rule — unlock hb subsequent lock of the same monitor.
 * 3. Volatile Variable Rule — volatile write hb subsequent volatile read.
 * 4. Thread Start Rule — Thread.start() hb any action in the started thread.
 * 5. Thread Join Rule — all actions in T hb Thread.join(T) returning.
 * 6. Transitivity — if A hb B and B hb C, then A hb C.
 *
 * @see README_5.2.1.md
 */
public class HappensBeforeDefinition {

    // ── Rule 1: Program Order Rule ────────────────────────────────────────────

    /**
     * Demonstrates the program order rule within a single thread.
     * Every action happens-before every subsequent action in the same thread.
     * The results here are deterministic because the program order rule guarantees
     * that x is set before the return.
     */
    public static class ProgramOrderDemo {
        public int computeWithOrdering() {
            int x = 1;   // (A) x=1 happens-before (B)
            int y = 2;   // (B) y=2 happens-before (C)
            return x + y; // (C) always sees x=1, y=2
        }
    }

    // ── Rule 2: Monitor Lock Rule ─────────────────────────────────────────────

    /**
     * Demonstrates the monitor lock rule: unlock of monitor M happens-before
     * every subsequent lock of M. The shared value written by the writer thread
     * is guaranteed to be visible to the reader thread once the reader acquires
     * the same monitor.
     */
    public static class MonitorLockDemo {
        private final Object monitor = new Object();
        private int sharedValue = 0;

        /**
         * Writer: acquires monitor, writes value, releases monitor.
         * Unlock of monitor happens-before any subsequent lock of the same monitor.
         */
        public void write(int value) {
            synchronized (monitor) {
                sharedValue = value; // (W) — visible after monitor unlock
            } // unlock of monitor
        }

        /**
         * Reader: acquires same monitor, reads value.
         * Because lock happens-after the writer's unlock, the monitor lock rule
         * guarantees sharedValue is visible with the value written by write().
         */
        public int read() {
            synchronized (monitor) { // lock of monitor — happens-after unlock
                return sharedValue;  // guaranteed to see written value
            }
        }
    }

    // ── Rule 3: Volatile Variable Rule ───────────────────────────────────────

    /**
     * Demonstrates the volatile variable rule: a write to volatile V happens-before
     * every subsequent read of V. The non-volatile 'data' write is also visible via
     * transitivity (program order + volatile rule).
     */
    public static class VolatileRuleDemo {
        private int data = 0;           // non-volatile, made visible via transitivity
        private volatile int vFlag = 0; // the synchronization point

        /**
         * Writer sets data then raises the flag.
         * Program order: data write hb vFlag write.
         * Volatile rule: vFlag write hb every subsequent vFlag read.
         */
        public void produce(int value) {
            data = value;   // (W1) program order: W1 hb W2
            vFlag = 1;      // (W2) volatile write
        }

        /**
         * Reader checks flag before reading data.
         * If vFlag == 1, by transitivity (W1 hb W2 hb R2 hb R1) data is visible.
         *
         * @return data value if flag is set, -1 otherwise.
         */
        public int consume() {
            if (vFlag == 1) { // (R2) volatile read: W2 hb R2
                return data;  // (R1) by transitivity: W1 hb W2 hb R2 hb R1, so W1 hb R1
            }
            return -1;
        }
    }

    // ── Rule 4: Thread Start Rule ─────────────────────────────────────────────

    /**
     * Demonstrates the thread start rule: Thread.start() happens-before any action
     * in the started thread. Writes before start() are guaranteed visible in the new thread.
     */
    public static class ThreadStartRuleDemo {
        private int preparedValue = 0;
        private final AtomicInteger observedValue = new AtomicInteger(-1);

        /**
         * Writes a value, then starts a thread. The thread start rule guarantees
         * the started thread will see preparedValue = value.
         *
         * @return an AtomicInteger that will hold the value observed by the thread.
         */
        public AtomicInteger demonstrateStartRule(int value) throws InterruptedException {
            preparedValue = value; // write BEFORE start()

            Thread t = new Thread(() -> {
                // Thread.start() hb this code — preparedValue is guaranteed visible
                observedValue.set(preparedValue);
            });

            t.start(); // start() hb thread body
            t.join();  // wait for thread to complete before returning
            return observedValue;
        }
    }

    // ── Rule 5: Thread Join Rule ──────────────────────────────────────────────

    /**
     * Demonstrates the thread join rule: all actions in thread T happen-before
     * Thread.join(T) returns. All writes by the joined thread are visible to
     * the joining thread after join() returns.
     */
    public static class ThreadJoinRuleDemo {
        private int result = 0;

        /**
         * Starts a thread that sets result, then joins it.
         * After join() returns, result is guaranteed to be the value set by the thread.
         */
        public int demonstrateJoinRule() throws InterruptedException {
            Thread worker = new Thread(() -> {
                result = 42; // (W) — all actions in worker hb join() returning
            });

            worker.start();
            worker.join(); // join() returns only after worker completes
            // join() hb this point — result=42 is guaranteed visible here
            return result;
        }
    }

    // ── Rule 6: Transitivity ──────────────────────────────────────────────────

    /**
     * Demonstrates transitivity: if A hb B and B hb C, then A hb C.
     * A non-volatile write can be made visible across threads via a volatile
     * synchronization point (piggybacking).
     */
    public static class TransitivityDemo {
        private int nonVolatileData = 0;      // (A) non-volatile
        private volatile int volatileFlag = 0; // (B) volatile synchronization point

        /**
         * Producer: non-volatile write (A), then volatile write (B).
         * By program order: A hb B.
         * By volatile rule: B hb any subsequent read of volatileFlag.
         * By transitivity: A hb (any read of volatileFlag) hb (read of nonVolatileData).
         */
        public void produce(int value) {
            nonVolatileData = value; // (A) program order: A hb B
            volatileFlag = 1;        // (B) volatile write: B hb D
        }

        /**
         * Consumer: volatile read (D), then non-volatile read (C).
         * If volatileFlag == 1: by program order D hb C, by transitivity A hb B hb D hb C.
         * So A hb C — nonVolatileData is visible with the value set in produce().
         */
        public int consume() {
            int flag = volatileFlag;     // (D) volatile read: B hb D
            if (flag == 1) {
                return nonVolatileData;  // (C) program order: D hb C; transitivity: A hb C
            }
            return -1;
        }
    }

    // ── Initialization-on-Demand Holder (Static Initializer hb) ───────────────

    /**
     * Demonstrates the static initializer happens-before guarantee.
     * A class's static initializer happens-before the first access to any static
     * field or method of that class. This is the basis of the Initialization-on-Demand
     * Holder (IODH) singleton pattern.
     */
    public static class IoDHSingleton {
        private final int id;

        private IoDHSingleton() {
            this.id = 42; // initialized in static init — always visible
        }

        /**
         * Holder class: loaded only when getInstance() is first called.
         * Static initializer runs under JVM class-loading lock, establishing
         * happens-before for any subsequent access to INSTANCE.
         */
        private static final class Holder {
            static final IoDHSingleton INSTANCE = new IoDHSingleton();
        }

        /**
         * Thread-safe lazy initialization without explicit synchronization.
         * The class-loading lock provides the necessary happens-before guarantee.
         */
        public static IoDHSingleton getInstance() {
            return Holder.INSTANCE;
        }

        public int getId() {
            return id;
        }
    }

    // ── Interrupt Happens-before ───────────────────────────────────────────────

    /**
     * Demonstrates the interrupt happens-before rule:
     * Thread.interrupt() happens-before the interrupted thread detects the interruption.
     */
    public static class InterruptHappensBefore {
        private volatile String messageBeforeInterrupt = null;
        private final AtomicReference<String> observedMessage = new AtomicReference<>();
        private final CountDownLatch interruptDetected = new CountDownLatch(1);

        public void setMessageBeforeInterrupt(String msg) {
            messageBeforeInterrupt = msg;
        }

        /**
         * Creates a thread that waits for an interrupt, then reads a value set before interrupt().
         * The interrupt hb guarantee ensures the thread sees messageBeforeInterrupt.
         */
        public Thread createInterruptibleThread() {
            return new Thread(() -> {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    // interrupt() hb InterruptedException — messageBeforeInterrupt is visible
                    observedMessage.set(messageBeforeInterrupt);
                    interruptDetected.countDown();
                }
            });
        }

        public AtomicReference<String> getObservedMessage() {
            return observedMessage;
        }

        public CountDownLatch getInterruptDetected() {
            return interruptDetected;
        }
    }

    // ── 64-bit long/double atomic write example ───────────────────────────────

    /**
     * Demonstrates why volatile is needed for long/double fields when accessed
     * from multiple threads. Non-volatile long writes may not be atomic on 32-bit JVMs.
     */
    public static class LongAtomicityDemo {
        volatile long safeValue = 0L;     // guaranteed atomic read/write
        long unsafeValue = 0L;            // may be non-atomic on 32-bit JVMs (word tearing)

        public void writeSafe(long v) { safeValue = v; }
        public long readSafe() { return safeValue; }
    }
}
