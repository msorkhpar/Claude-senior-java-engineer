package practice;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** A counter guarded by the lock it is given. */
public final class TimedCounter {

    private final ReentrantLock lock;
    private long count;

    public TimedCounter(ReentrantLock lock) {
        this.lock = lock;
    }

    /** Adds one only if the lock is free right now; returns whether it counted. */
    public boolean tryIncrement() {
        if (!lock.tryLock()) {
            return false;
        }
        try {
            count++;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Waits at most {@code timeout} for the lock; returns whether it counted. */
    public boolean increment(long timeout, TimeUnit unit) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            count++;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** The count, read under the lock. */
    public long count() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    /** Whether at least one thread is waiting for the lock. */
    public boolean isContended() {
        return lock.hasQueuedThreads();
    }
}
