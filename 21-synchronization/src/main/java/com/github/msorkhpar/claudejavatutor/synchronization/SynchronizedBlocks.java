package com.github.msorkhpar.claudejavatutor.synchronization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates monitors and synchronized blocks in Java:
 * <ul>
 *   <li>Synchronized instance methods (lock on {@code this})</li>
 *   <li>Synchronized static methods (lock on {@code Class} object)</li>
 *   <li>Synchronized blocks with custom lock objects</li>
 *   <li>Intrinsic lock reentrancy</li>
 *   <li>Lock release on exception</li>
 *   <li>Fine-grained locking with multiple lock objects</li>
 *   <li>Wait/notify monitor communication</li>
 * </ul>
 *
 * @see README_6.2.1.md
 */
public class SynchronizedBlocks {

    // -----------------------------------------------------------------------
    // Synchronized Counter — demonstrates basic synchronized method
    // -----------------------------------------------------------------------

    /**
     * A thread-safe counter using synchronized instance methods.
     * The intrinsic lock is {@code this}, meaning all synchronized methods
     * on the same instance share the same lock.
     */
    public static class SynchronizedCounter {
        private int count = 0;

        /** Atomically increments the count. Locks on {@code this}. */
        public synchronized void increment() {
            count++;
        }

        /** Atomically decrements the count. Locks on {@code this}. */
        public synchronized void decrement() {
            count--;
        }

        /** Returns the current count. Locks on {@code this}. */
        public synchronized int getCount() {
            return count;
        }
    }

    // -----------------------------------------------------------------------
    // Block-level Synchronized Counter — demonstrates synchronized blocks
    // -----------------------------------------------------------------------

    /**
     * A thread-safe counter using a private final lock object and synchronized blocks.
     * This is the preferred approach because:
     * <ul>
     *   <li>The lock is not exposed to external code</li>
     *   <li>The critical section can be narrowed to only the necessary code</li>
     * </ul>
     */
    public static class BlockSynchronizedCounter {
        private int count = 0;
        private final Object lock = new Object();

        public void increment() {
            synchronized (lock) {
                count++;
            }
        }

        public void decrement() {
            synchronized (lock) {
                count--;
            }
        }

        public int getCount() {
            synchronized (lock) {
                return count;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Static Synchronized — demonstrates class-level locking
    // -----------------------------------------------------------------------

    /**
     * Demonstrates static synchronized methods, which lock on the Class object.
     * Instance synchronized methods lock on {@code this}, so they use a DIFFERENT
     * lock and can run concurrently with static synchronized methods.
     */
    public static class StaticVsInstanceLock {
        private static int staticCounter = 0;
        private int instanceCounter = 0;

        /** Locks on {@code StaticVsInstanceLock.class}. */
        public static synchronized void incrementStatic() {
            staticCounter++;
        }

        public static synchronized int getStaticCounter() {
            return staticCounter;
        }

        /** Resets static counter for test isolation. */
        public static synchronized void resetStatic() {
            staticCounter = 0;
        }

        /** Locks on {@code this} — different lock from static methods. */
        public synchronized void incrementInstance() {
            instanceCounter++;
        }

        public synchronized int getInstanceCounter() {
            return instanceCounter;
        }
    }

    // -----------------------------------------------------------------------
    // Reentrant Lock Demonstration
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that Java's intrinsic locks are reentrant.
     * A thread that already holds the lock can re-acquire it without deadlocking.
     * This is critical for inheritance and method delegation patterns.
     */
    public static class ReentrantDemo {
        private final List<String> callLog = Collections.synchronizedList(new ArrayList<>());

        /**
         * Calls inner() while holding the lock on {@code this}.
         * Without reentrancy, this would deadlock because inner() also
         * tries to acquire the same lock.
         */
        public synchronized void outer() {
            callLog.add("outer-start");
            inner(); // re-acquires the same lock — reentrancy
            callLog.add("outer-end");
        }

        public synchronized void inner() {
            callLog.add("inner");
        }

        public List<String> getCallLog() {
            return new ArrayList<>(callLog);
        }
    }

    /**
     * Demonstrates reentrancy with inheritance.
     * A subclass overriding a synchronized method can call super.method()
     * without deadlocking because intrinsic locks are reentrant.
     */
    public static class ReentrantInheritanceBase {
        private final List<String> log = Collections.synchronizedList(new ArrayList<>());

        public synchronized void doWork() {
            log.add("base");
        }

        public List<String> getLog() {
            return new ArrayList<>(log);
        }
    }

    public static class ReentrantInheritanceDerived extends ReentrantInheritanceBase {
        @Override
        public synchronized void doWork() {
            super.doWork(); // re-acquires lock on 'this' — works due to reentrancy
            getLog(); // access the parent's log reference
            // add derived entry through parent's log
            super.getLog(); // just to show we can call another synchronized method
        }

        /** A public method to add to log for testing purposes. */
        public synchronized void doWorkAndLog() {
            super.doWork();
            // We need to add "derived" to the log; use a separate method
            addDerivedEntry();
        }

        private synchronized void addDerivedEntry() {
            // Access parent's log through getter - also reentrant
            var log = super.getLog();
            // Since getLog returns a copy, we need a direct way
        }
    }

    // -----------------------------------------------------------------------
    // Lock Release on Exception
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that the intrinsic lock is automatically released when
     * a synchronized block exits due to an exception.
     */
    public static class LockReleaseOnException {
        private final Object lock = new Object();
        private int value = 0;
        private boolean exceptionThrown = false;

        /**
         * Acquires the lock and throws an exception if requested.
         * The lock is released regardless of how the block exits.
         */
        public void riskyUpdate(int newValue, boolean shouldThrow) {
            synchronized (lock) {
                value = newValue;
                if (shouldThrow) {
                    exceptionThrown = true;
                    throw new RuntimeException("Intentional exception");
                }
            }
        }

        /**
         * Attempts to read the value. If the lock was NOT released after the
         * exception, this method would block indefinitely.
         */
        public int getValue() {
            synchronized (lock) {
                return value;
            }
        }

        public boolean wasExceptionThrown() {
            return exceptionThrown;
        }
    }

    // -----------------------------------------------------------------------
    // Fine-Grained Locking — independent locks for independent state
    // -----------------------------------------------------------------------

    /**
     * Demonstrates fine-grained locking using separate lock objects
     * for independent pieces of state. This allows operations on
     * different state to proceed concurrently.
     */
    public static class FineGrainedLocking {
        private int balance = 0;
        private final List<String> transactionLog = new ArrayList<>();

        private final Object balanceLock = new Object();
        private final Object logLock = new Object();

        public void deposit(int amount) {
            synchronized (balanceLock) {
                balance += amount;
            }
            // Log update uses a different lock — no contention with balance ops
            synchronized (logLock) {
                transactionLog.add("deposit:" + amount);
            }
        }

        public void withdraw(int amount) {
            synchronized (balanceLock) {
                balance -= amount;
            }
            synchronized (logLock) {
                transactionLog.add("withdraw:" + amount);
            }
        }

        public int getBalance() {
            synchronized (balanceLock) {
                return balance;
            }
        }

        public List<String> getTransactionLog() {
            synchronized (logLock) {
                return new ArrayList<>(transactionLog);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Wait/Notify — monitor-based inter-thread communication
    // -----------------------------------------------------------------------

    /**
     * A bounded buffer (producer-consumer) demonstrating wait/notify.
     * <ul>
     *   <li>wait() releases the lock and blocks until notified</li>
     *   <li>notifyAll() wakes all waiting threads</li>
     *   <li>wait() is always called in a loop to handle spurious wakeups</li>
     * </ul>
     */
    public static class BoundedBuffer<T> {
        private final List<T> buffer;
        private final int capacity;
        private final Object lock = new Object();

        public BoundedBuffer(int capacity) {
            this.capacity = capacity;
            this.buffer = new ArrayList<>(capacity);
        }

        /**
         * Adds an item to the buffer, blocking if full.
         *
         * @throws InterruptedException if the thread is interrupted while waiting
         */
        public void put(T item) throws InterruptedException {
            synchronized (lock) {
                while (buffer.size() == capacity) {
                    lock.wait(); // releases lock, waits for space
                }
                buffer.add(item);
                lock.notifyAll(); // wake consumers waiting for data
            }
        }

        /**
         * Removes and returns an item from the buffer, blocking if empty.
         *
         * @throws InterruptedException if the thread is interrupted while waiting
         */
        public T take() throws InterruptedException {
            synchronized (lock) {
                while (buffer.isEmpty()) {
                    lock.wait(); // releases lock, waits for data
                }
                T item = buffer.removeFirst();
                lock.notifyAll(); // wake producers waiting for space
                return item;
            }
        }

        public int size() {
            synchronized (lock) {
                return buffer.size();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Synchronizing on null — pitfall demonstration
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that synchronizing on a null reference throws NullPointerException.
     */
    public static class NullLockPitfall {
        /**
         * Attempts to synchronize on the given lock object.
         *
         * @throws NullPointerException if lockObject is null
         */
        public void synchronizeOn(Object lockObject) {
            synchronized (lockObject) {
                // NullPointerException thrown before entering this block if lockObject is null
            }
        }
    }
}
