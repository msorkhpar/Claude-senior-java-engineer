package practice;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntUnaryOperator;

public final class Balance {

    private final ReentrantLock lock = new ReentrantLock();
    private int value;

    public Balance(int initial) {
        this.value = initial;
    }

    /** Applies {@code change} to the balance, one update at a time, and returns the new balance. */
    public int update(IntUnaryOperator change) {
        lock.lock();
        value = change.applyAsInt(value);
        int now = value;
        lock.unlock();
        return now;
    }

    /** Returns the balance. */
    public int get() {
        lock.lock();
        try {
            return value;
        } finally {
            lock.unlock();
        }
    }
}
