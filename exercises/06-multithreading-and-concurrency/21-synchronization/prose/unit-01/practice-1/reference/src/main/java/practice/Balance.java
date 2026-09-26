package practice;

import java.util.function.IntUnaryOperator;

public final class Balance {

    private final Object lock = new Object();
    private int value;

    public Balance(int initial) {
        this.value = initial;
    }

    /** Applies {@code change} to the balance, one update at a time, and returns the new balance. */
    public int update(IntUnaryOperator change) {
        synchronized (lock) {
            value = change.applyAsInt(value);
            return value;
        }
    }

    /** Returns the balance. */
    public int get() {
        synchronized (lock) {
            return value;
        }
    }
}
