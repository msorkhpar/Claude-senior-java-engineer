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
        throw new UnsupportedOperationException("write tryIncrement");
    }

    /** Waits at most {@code timeout} for the lock; returns whether it counted. */
    public boolean increment(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("write increment");
    }

    /** The count, read under the lock. */
    public long count() {
        throw new UnsupportedOperationException("write count");
    }

    /** Whether at least one thread is waiting for the lock. */
    public boolean isContended() {
        throw new UnsupportedOperationException("write isContended");
    }
}
