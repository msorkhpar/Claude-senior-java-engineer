package practice;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class TimedCounter {
    private final ReentrantLock lock;
    private long count;

    public TimedCounter(ReentrantLock lock) { this.lock = lock; }

    public boolean tryIncrement() {
        if (lock.isLocked()) return false;
        lock.lock();
        try { count++; return true; } finally { lock.unlock(); }
    }

    public boolean increment(long timeout, TimeUnit unit) throws InterruptedException {
        if (!lock.tryLock(timeout, unit)) return false;
        try { count++; return true; } finally { lock.unlock(); }
    }

    public long count() {
        lock.lock();
        try { return count; } finally { lock.unlock(); }
    }

    public boolean isContended() { return lock.hasQueuedThreads(); }
}
