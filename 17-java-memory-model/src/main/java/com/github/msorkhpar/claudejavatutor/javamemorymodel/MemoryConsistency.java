package com.github.msorkhpar.claudejavatutor.javamemorymodel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates synchronization and memory consistency in the Java Memory Model:
 * <ul>
 *   <li>Synchronized happens-before: monitor release/acquire pairs</li>
 *   <li>Double-checked locking with volatile (correct pattern)</li>
 *   <li>ReentrantLock usage</li>
 *   <li>Wait/notify for inter-thread coordination</li>
 *   <li>Reentrant locking</li>
 * </ul>
 *
 * @see README_5.1.3.md
 */
public class MemoryConsistency {

    // -----------------------------------------------------------------------
    // Synchronized Happens-Before
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that a synchronized write on a monitor happens-before a subsequent
     * synchronized read on the same monitor, ensuring memory consistency.
     */
    public static class SynchronizedWriter {
        private final Object lock = new Object();
        private int value = 0;
        private String name = null;

        /** Writes value and name under the lock. */
        public void write(int value, String name) {
            synchronized (lock) {
                this.value = value;  // (1) written under lock
                this.name = name;    // (2) written under lock
            }                        // (3) monitor release — flushes (1) and (2)
        }

        /** Reads value and name under the lock — sees writes from write() if released first. */
        public Values read() {
            synchronized (lock) {   // (4) monitor acquire — happens-after (3)
                return new Values(value, name);
            }
        }

        public record Values(int value, String name) {}
    }

    /**
     * Demonstrates synchronized increment — atomic and visible across threads.
     */
    public static class OrderedCounter {
        private int count = 0;
        private final Object lock = new Object();

        public void increment() {
            synchronized (lock) {
                count++; // compound op protected by lock
            }
        }

        public int get() {
            synchronized (lock) {
                return count;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Double-Checked Locking (correct, with volatile)
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the correct double-checked locking pattern (Java 5+).
     * The {@code volatile} keyword on {@code instance} prevents the reordering of
     * object construction steps with the assignment of the reference, ensuring
     * any thread that sees a non-null instance also sees the fully initialized object.
     */
    public static class DCLSingleton {
        public static final int EXPECTED_VALUE = 42;

        private static volatile DCLSingleton instance; // CRITICAL: volatile

        private final int value;

        private DCLSingleton() {
            this.value = EXPECTED_VALUE; // constructor initialization
        }

        public static DCLSingleton getInstance() {
            if (instance == null) {                // First check — no lock (fast path)
                synchronized (DCLSingleton.class) {
                    if (instance == null) {         // Second check — with lock
                        instance = new DCLSingleton(); // volatile write — prevents reordering
                    }
                }
            }
            return instance; // volatile read
        }

        public int getValue() { return value; }
    }

    // -----------------------------------------------------------------------
    // Synchronized state updates
    // -----------------------------------------------------------------------

    /**
     * Demonstrates synchronized state with multiple fields updated atomically.
     */
    public static class SynchronizedState {
        private int count = 0;
        private String label = "";
        private final Object lock = new Object();

        public void update(int count, String label) {
            synchronized (lock) {
                this.count = count;
                this.label = label;
            }
        }

        public Snapshot snapshot() {
            synchronized (lock) {
                return new Snapshot(count, label);
            }
        }

        public record Snapshot(int count, String label) {}
    }

    /**
     * Thread-safe accumulator using synchronized for correct multi-thread addition.
     */
    public static class ConcurrentAccumulator {
        private long total = 0;
        private final Object lock = new Object();

        public void add(long value) {
            synchronized (lock) {
                total += value;
            }
        }

        public long getTotal() {
            synchronized (lock) {
                return total;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Reentrant locking
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that Java's intrinsic lock (synchronized) is reentrant:
     * a thread already holding the lock can re-acquire it without deadlocking.
     */
    public static class ReentrantExample {
        public static final int EXPECTED_RESULT = 10;

        public synchronized int outer() {
            // This thread holds the lock; calling inner() re-acquires it — no deadlock
            return inner() + 5;
        }

        public synchronized int inner() {
            return 5; // re-acquires the lock already held by the same thread
        }
    }

    /**
     * Thread-safe counter using {@link ReentrantLock} instead of {@code synchronized}.
     * ReentrantLock offers the same memory semantics as synchronized, plus features
     * like try-lock, timed lock, and condition variables.
     */
    public static class ReentrantLockCounter {
        private int count = 0;
        private final ReentrantLock lock = new ReentrantLock();

        public void increment() {
            lock.lock();
            try {
                count++; // protected by ReentrantLock
            } finally {
                lock.unlock(); // always unlock in finally to prevent lock leaks
            }
        }

        public int get() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Wait/Notify — correct pattern with while loop (prevents spurious wakeup)
    // -----------------------------------------------------------------------

    /**
     * A simple bounded producer/consumer queue demonstrating correct use of
     * {@code wait()} inside a {@code while} loop to guard against spurious wakeups.
     *
     * <p>The JMM guarantees that:
     * <ol>
     *   <li>The write to the queue inside the {@code put()} synchronized block
     *       happens-before the {@code notifyAll()} call.</li>
     *   <li>The {@code notifyAll()} happens-before the return from {@code wait()} in the
     *       consuming thread.</li>
     *   <li>Therefore, {@code take()} sees the item written by {@code put()}.</li>
     * </ol>
     */
    public static class ProducerConsumerQueue {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity = 5;
        private final Object lock = new Object();

        /**
         * Puts an item into the queue, blocking if the queue is full.
         * Uses {@code while} loop to handle spurious wakeups.
         */
        public void put(int item) throws InterruptedException {
            synchronized (lock) {
                while (queue.size() == capacity) { // while — not if — for spurious wakeups
                    lock.wait();
                }
                queue.offer(item);
                lock.notifyAll(); // wake all waiting consumers
            }
        }

        /**
         * Takes an item from the queue, blocking if empty.
         * Uses {@code while} loop to handle spurious wakeups.
         */
        public int take() throws InterruptedException {
            synchronized (lock) {
                while (queue.isEmpty()) { // while — not if — for spurious wakeups
                    lock.wait();
                }
                int item = queue.poll();
                lock.notifyAll(); // wake all waiting producers
                return item;
            }
        }

        public int size() {
            synchronized (lock) {
                return queue.size();
            }
        }
    }
}
