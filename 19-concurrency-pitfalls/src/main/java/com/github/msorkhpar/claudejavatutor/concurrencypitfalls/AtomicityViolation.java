package com.github.msorkhpar.claudejavatutor.concurrencypitfalls;

import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates atomicity violations and techniques to ensure atomic operations.
 * <p>
 * An atomicity violation occurs when a compound operation (read-modify-write, check-then-act,
 * or multi-variable update) is interrupted midway by another thread, leaving the system in
 * an inconsistent state.
 * <p>
 * Patterns covered:
 * <ul>
 *   <li>Non-atomic compound operations vs AtomicInteger</li>
 *   <li>Multi-variable invariant violations vs synchronized / immutable snapshot</li>
 *   <li>CAS retry loops</li>
 *   <li>ABA problem and AtomicStampedReference</li>
 *   <li>LongAdder vs AtomicLong for high-contention counters</li>
 * </ul>
 *
 * @see README_5.3.2.md
 */
public class AtomicityViolation {

    // ── Pitfall 1: Non-Atomic Compound Operation ─────────────────────────────

    /**
     * UNSAFE: counter++ is not atomic. It decomposes into read, increment, write.
     * Under concurrent access, updates will be lost.
     */
    public static class NonAtomicIncrement {
        private int counter = 0;

        public void increment() {
            counter++;
        }

        public int get() {
            return counter;
        }
    }

    /**
     * Thread-safe increment using AtomicInteger with CAS-based operations.
     */
    public static class AtomicIncrement {
        private final AtomicInteger counter = new AtomicInteger(0);

        public void increment() {
            counter.incrementAndGet();
        }

        public int get() {
            return counter.get();
        }

        /**
         * Atomically increment only if the current value is below a given max.
         * Demonstrates a CAS retry loop for conditional update.
         */
        public boolean incrementIfBelow(int max) {
            int current;
            do {
                current = counter.get();
                if (current >= max) return false;
            } while (!counter.compareAndSet(current, current + 1));
            return true;
        }
    }

    // ── Pitfall 2: Multi-Variable Invariant Violation ────────────────────────

    /**
     * UNSAFE: x and y must represent a consistent coordinate, but they are updated
     * without synchronization. A reader may see x from one write and y from another.
     */
    public static class NonAtomicCoordinate {
        private int x = 0;
        private int y = 0;

        public void moveTo(int newX, int newY) {
            x = newX;
            y = newY;
        }

        public int[] getPosition() {
            return new int[]{x, y};
        }
    }

    /**
     * Thread-safe coordinate using synchronized methods.
     * All accesses go through the same monitor, ensuring atomicity of multi-variable updates.
     */
    public static class SynchronizedCoordinate {
        private int x = 0;
        private int y = 0;

        public synchronized void moveTo(int newX, int newY) {
            x = newX;
            y = newY;
        }

        public synchronized int[] getPosition() {
            return new int[]{x, y};
        }
    }

    /**
     * Thread-safe coordinate using an immutable record + volatile reference.
     * Writers create a new snapshot; readers get a consistent snapshot via volatile read.
     * Lock-free and excellent for read-heavy workloads.
     */
    public static class ImmutableCoordinate {
        public record Point(int x, int y) {}

        private volatile Point position = new Point(0, 0);

        public void moveTo(int newX, int newY) {
            position = new Point(newX, newY);
        }

        public Point getPosition() {
            return position;
        }
    }

    // ── Pitfall 3: Non-Atomic Bank Transfer ──────────────────────────────────

    /**
     * Demonstrates an atomicity violation with a bank account.
     * Deposit and withdraw update balance and transaction count as two separate operations.
     */
    public static class UnsafeBankAccount {
        private int balance = 0;
        private int transactionCount = 0;

        public void deposit(int amount) {
            balance += amount;
            transactionCount++;
        }

        public void withdraw(int amount) {
            balance -= amount;
            transactionCount++;
        }

        public int getBalance() {
            return balance;
        }

        public int getTransactionCount() {
            return transactionCount;
        }
    }

    /**
     * Thread-safe bank account using ReentrantLock.
     * All operations are atomic -- balance and transactionCount are always consistent.
     */
    public static class SafeBankAccount {
        private final ReentrantLock lock = new ReentrantLock();
        private int balance = 0;
        private int transactionCount = 0;

        public void deposit(int amount) {
            lock.lock();
            try {
                balance += amount;
                transactionCount++;
            } finally {
                lock.unlock();
            }
        }

        public void withdraw(int amount) {
            lock.lock();
            try {
                if (balance < amount) {
                    throw new IllegalStateException("Insufficient funds: balance=" + balance + ", requested=" + amount);
                }
                balance -= amount;
                transactionCount++;
            } finally {
                lock.unlock();
            }
        }

        public int getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }

        public int getTransactionCount() {
            lock.lock();
            try {
                return transactionCount;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Returns a consistent snapshot of balance and transaction count.
         */
        public int[] getSnapshot() {
            lock.lock();
            try {
                return new int[]{balance, transactionCount};
            } finally {
                lock.unlock();
            }
        }
    }

    // ── CAS Retry Loop ───────────────────────────────────────────────────────

    /**
     * Demonstrates a CAS retry loop for custom atomic operations.
     */
    public static class CASRetryCounter {
        private final AtomicInteger value = new AtomicInteger(0);

        /**
         * Atomically add n to the value using a CAS retry loop.
         */
        public void addN(int n) {
            int current;
            do {
                current = value.get();
            } while (!value.compareAndSet(current, current + n));
        }

        /**
         * Atomically increment only if positive; returns whether the increment happened.
         */
        public boolean incrementIfPositive() {
            int current;
            do {
                current = value.get();
                if (current <= 0) return false;
            } while (!value.compareAndSet(current, current + 1));
            return true;
        }

        public int get() {
            return value.get();
        }

        public void set(int newValue) {
            value.set(newValue);
        }
    }

    // ── ABA Problem and AtomicStampedReference ───────────────────────────────

    /**
     * Demonstrates the ABA problem and its solution with AtomicStampedReference.
     */
    public static class ABADemo {
        private final AtomicStampedReference<String> stampedRef;

        public ABADemo(String initialValue) {
            this.stampedRef = new AtomicStampedReference<>(initialValue, 0);
        }

        /**
         * Reads the current value and its stamp (version).
         */
        public String getValue() {
            return stampedRef.getReference();
        }

        public int getStamp() {
            return stampedRef.getStamp();
        }

        /**
         * Atomically sets the value if the current reference and stamp match the expected ones.
         * The stamp is incremented to prevent ABA issues.
         */
        public boolean compareAndSet(String expectedValue, int expectedStamp, String newValue) {
            return stampedRef.compareAndSet(expectedValue, newValue, expectedStamp, expectedStamp + 1);
        }

        /**
         * Reads both value and stamp atomically.
         */
        public StampedValue<String> getStampedValue() {
            int[] stampHolder = new int[1];
            String value = stampedRef.get(stampHolder);
            return new StampedValue<>(value, stampHolder[0]);
        }
    }

    public record StampedValue<T>(T value, int stamp) {}

    // ── LongAdder for High-Contention Counters ───────────────────────────────

    /**
     * Demonstrates LongAdder for high-contention scenarios.
     * LongAdder uses internal striping to reduce CAS contention,
     * outperforming AtomicLong under heavy write load.
     */
    public static class HighContentionCounter {
        private final java.util.concurrent.atomic.LongAdder adder = new java.util.concurrent.atomic.LongAdder();

        public void increment() {
            adder.increment();
        }

        public void add(long delta) {
            adder.add(delta);
        }

        public long sum() {
            return adder.sum();
        }

        public void reset() {
            adder.reset();
        }

        /**
         * Returns the sum and resets atomically.
         */
        public long sumThenReset() {
            return adder.sumThenReset();
        }
    }

    /**
     * Demonstrates LongAccumulator for custom binary operations.
     */
    public static class MaxAccumulator {
        private final java.util.concurrent.atomic.LongAccumulator accumulator =
                new java.util.concurrent.atomic.LongAccumulator(Long::max, Long.MIN_VALUE);

        public void observe(long value) {
            accumulator.accumulate(value);
        }

        public long getMax() {
            return accumulator.get();
        }

        public void reset() {
            accumulator.reset();
        }
    }
}
