package com.github.msorkhpar.claudejavatutor.synchronization;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates deadlock scenarios and prevention strategies:
 * <ul>
 *   <li>Classic deadlock with inconsistent lock ordering</li>
 *   <li>Deadlock prevention via consistent lock ordering</li>
 *   <li>Deadlock prevention via tryLock with timeout</li>
 *   <li>Livelock scenario</li>
 * </ul>
 *
 * @see README_6.2.1.md
 */
public class DeadlockPrevention {

    // -----------------------------------------------------------------------
    // Bank Account — demonstrates lock ordering for deadlock prevention
    // -----------------------------------------------------------------------

    /**
     * A bank account that supports thread-safe transfers using consistent
     * lock ordering to prevent deadlock.
     *
     * <p>Without lock ordering, two threads performing:
     * <pre>
     *   Thread A: transfer(account1, account2, 100)
     *   Thread B: transfer(account2, account1, 50)
     * </pre>
     * could deadlock (A holds lock1, waits for lock2; B holds lock2, waits for lock1).
     *
     * <p>With lock ordering (always acquire the lock with the lower ID first),
     * both threads will attempt to acquire the same lock first, preventing
     * circular wait.
     */
    public static class BankAccount {
        private final long id;
        private int balance;
        private final Object lock = new Object();

        public BankAccount(long id, int initialBalance) {
            this.id = id;
            this.balance = initialBalance;
        }

        public long getId() {
            return id;
        }

        public int getBalance() {
            synchronized (lock) {
                return balance;
            }
        }

        /**
         * Transfers amount from {@code from} to {@code to} using consistent
         * lock ordering based on account ID. This prevents deadlock even when
         * two threads transfer in opposite directions simultaneously.
         *
         * @return true if the transfer succeeded, false if insufficient funds
         */
        public static boolean transfer(BankAccount from, BankAccount to, int amount) {
            // Determine lock order by account ID to prevent deadlock
            BankAccount first = from.id < to.id ? from : to;
            BankAccount second = from.id < to.id ? to : from;

            // Handle equal IDs (same account — should not transfer to self)
            if (from.id == to.id) {
                return false;
            }

            synchronized (first.lock) {
                synchronized (second.lock) {
                    if (from.balance >= amount) {
                        from.balance -= amount;
                        to.balance += amount;
                        return true;
                    }
                    return false;
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // TryLock-based Transfer — deadlock avoidance via timeout
    // -----------------------------------------------------------------------

    /**
     * A bank account using {@link ReentrantLock#tryLock(long, TimeUnit)} to avoid
     * deadlock. Instead of blocking indefinitely, the thread backs off if it
     * cannot acquire both locks within a timeout.
     */
    public static class TryLockAccount {
        private final long id;
        private int balance;
        private final ReentrantLock lock = new ReentrantLock();

        public TryLockAccount(long id, int initialBalance) {
            this.id = id;
            this.balance = initialBalance;
        }

        public long getId() {
            return id;
        }

        public int getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Attempts to transfer using tryLock with a timeout.
         * If both locks cannot be acquired together, the operation releases
         * any held lock and retries — avoiding deadlock through back-off.
         *
         * @param timeout  the maximum total time to keep retrying
         * @param unit     the time unit of the timeout
         * @return true if transfer succeeded, false if it could not acquire locks or insufficient funds
         * @throws InterruptedException if the current thread is interrupted
         */
        public static boolean transferWithTimeout(
                TryLockAccount from, TryLockAccount to, int amount,
                long timeout, TimeUnit unit) throws InterruptedException {

            long deadline = System.nanoTime() + unit.toNanos(timeout);

            while (System.nanoTime() < deadline) {
                if (from.lock.tryLock()) {
                    try {
                        if (to.lock.tryLock()) {
                            try {
                                if (from.balance >= amount) {
                                    from.balance -= amount;
                                    to.balance += amount;
                                    return true;
                                }
                                return false; // insufficient funds
                            } finally {
                                to.lock.unlock();
                            }
                        }
                    } finally {
                        from.lock.unlock();
                    }
                }
                // Back off briefly to let the other thread proceed
                Thread.onSpinWait();
            }
            return false; // timed out — could not acquire both locks
        }
    }

    // -----------------------------------------------------------------------
    // Deadlock Detector — demonstrates deadlock detection concept
    // -----------------------------------------------------------------------

    /**
     * A resource holder that intentionally creates a deadlock scenario for
     * demonstration and testing purposes.
     *
     * <p>Two threads each hold one lock and try to acquire the other's lock,
     * creating a classic circular-wait deadlock.
     */
    public static class DeadlockDemo {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();
        private volatile boolean thread1Started = false;
        private volatile boolean thread2Started = false;

        /**
         * Acquires lock1 then attempts to acquire lock2.
         * If another thread holds lock2 and is waiting for lock1, deadlock occurs.
         */
        public void methodA() {
            synchronized (lock1) {
                thread1Started = true;
                // Spin-wait for the other thread to start (to ensure deadlock)
                while (!thread2Started) {
                    Thread.onSpinWait();
                }
                synchronized (lock2) {
                    // This point is never reached in a deadlock scenario
                }
            }
        }

        /**
         * Acquires lock2 then attempts to acquire lock1 — opposite order from methodA.
         */
        public void methodB() {
            synchronized (lock2) {
                thread2Started = true;
                while (!thread1Started) {
                    Thread.onSpinWait();
                }
                synchronized (lock1) {
                    // This point is never reached in a deadlock scenario
                }
            }
        }

        public boolean isThread1Started() {
            return thread1Started;
        }

        public boolean isThread2Started() {
            return thread2Started;
        }
    }

    // -----------------------------------------------------------------------
    // Livelock Demonstration
    // -----------------------------------------------------------------------

    /**
     * Demonstrates a livelock scenario where two "polite" workers keep yielding
     * to each other without making progress.
     *
     * <p>Unlike deadlock (threads are blocked), in livelock the threads are active
     * but keep responding to each other in a way that prevents progress.
     */
    public static class LivelockDemo {
        private volatile String currentOwner;
        private volatile int retryCountWorker1 = 0;
        private volatile int retryCountWorker2 = 0;
        private final int maxRetries;

        public LivelockDemo(int maxRetries) {
            this.maxRetries = maxRetries;
            this.currentOwner = "worker1";
        }

        /**
         * Worker1 tries to use the resource but yields if worker2 needs it.
         * Returns true if the worker eventually completed its work.
         */
        public boolean worker1Work() {
            while (retryCountWorker1 < maxRetries) {
                if (!"worker1".equals(currentOwner)) {
                    retryCountWorker1++;
                    Thread.onSpinWait();
                    continue;
                }
                // "Politely" give up the resource to the other worker
                currentOwner = "worker2";
                retryCountWorker1++;
            }
            return false; // Never completed real work — livelock
        }

        /**
         * Worker2 tries to use the resource but yields if worker1 needs it.
         */
        public boolean worker2Work() {
            while (retryCountWorker2 < maxRetries) {
                if (!"worker2".equals(currentOwner)) {
                    retryCountWorker2++;
                    Thread.onSpinWait();
                    continue;
                }
                currentOwner = "worker1";
                retryCountWorker2++;
            }
            return false; // Never completed real work — livelock
        }

        public int getRetryCountWorker1() {
            return retryCountWorker1;
        }

        public int getRetryCountWorker2() {
            return retryCountWorker2;
        }
    }
}
