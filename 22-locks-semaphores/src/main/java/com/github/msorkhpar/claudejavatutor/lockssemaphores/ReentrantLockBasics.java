package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates the Lock interface and ReentrantLock class (6.3.1.1),
 * explicit locking and unlocking patterns (6.3.1.2), and reentrancy semantics.
 * <p>
 * Topics covered:
 * - Basic lock/unlock with try-finally
 * - Reentrancy and hold count
 * - tryLock() (non-blocking and timed)
 * - lockInterruptibly()
 * - Fair vs non-fair locks
 * - Diagnostic methods: isLocked(), isHeldByCurrentThread(), getQueueLength()
 */
public class ReentrantLockBasics {

    // -----------------------------------------------------------------------
    // Thread-safe counter using ReentrantLock
    // -----------------------------------------------------------------------

    private final ReentrantLock lock;
    private long count;

    public ReentrantLockBasics() {
        this(false);
    }

    public ReentrantLockBasics(boolean fair) {
        this.lock = new ReentrantLock(fair);
        this.count = 0;
    }

    /**
     * Increments the counter using lock/try-finally/unlock pattern.
     * Demonstrates the canonical explicit locking idiom.
     */
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current count, safely reading under lock.
     */
    public long getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts a non-blocking increment using tryLock().
     *
     * @return true if lock was acquired and increment performed, false otherwise
     */
    public boolean tryIncrement() {
        if (lock.tryLock()) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * Attempts a timed increment using tryLock(time, unit).
     *
     * @param timeout  maximum time to wait for the lock
     * @param unit     time unit
     * @return true if lock was acquired within timeout, false otherwise
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean timedIncrement(long timeout, TimeUnit unit) throws InterruptedException {
        if (lock.tryLock(timeout, unit)) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * Increments the counter using lockInterruptibly(), which allows the thread
     * to respond to interruption while waiting for the lock.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void interruptibleIncrement() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    // -----------------------------------------------------------------------
    // Reentrancy demonstration
    // -----------------------------------------------------------------------

    /**
     * Demonstrates reentrancy: calling inner() from within a locked section.
     * The same thread re-acquires the lock without deadlocking.
     *
     * @return the hold count observed inside the inner method
     */
    public int demonstrateReentrancy() {
        lock.lock();
        try {
            // Hold count is 1 here
            return reentrantInner();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called from within a locked section. Re-acquires the same lock (reentrancy).
     *
     * @return the hold count inside this method
     */
    private int reentrantInner() {
        lock.lock();
        try {
            // Hold count is 2 here (same thread re-entered)
            return lock.getHoldCount();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current hold count of the lock by the calling thread.
     */
    public int getHoldCount() {
        return lock.getHoldCount();
    }

    // -----------------------------------------------------------------------
    // Diagnostic methods
    // -----------------------------------------------------------------------

    /**
     * Returns true if the lock is currently held by any thread.
     */
    public boolean isLocked() {
        return lock.isLocked();
    }

    /**
     * Returns true if the lock is held by the current thread.
     */
    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    /**
     * Returns true if the lock uses fair ordering.
     */
    public boolean isFair() {
        return lock.isFair();
    }

    /**
     * Returns the estimated number of threads waiting to acquire this lock.
     */
    public int getQueueLength() {
        return lock.getQueueLength();
    }

    /**
     * Returns true if there are threads waiting to acquire this lock.
     */
    public boolean hasQueuedThreads() {
        return lock.hasQueuedThreads();
    }

    /**
     * Exposes the underlying lock for advanced testing scenarios.
     */
    ReentrantLock getLock() {
        return lock;
    }
}
