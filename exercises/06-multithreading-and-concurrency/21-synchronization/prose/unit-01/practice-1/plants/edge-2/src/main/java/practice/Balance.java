package practice;

import java.util.function.IntUnaryOperator;

public final class Balance {

    private int value;

    public Balance(int initial) {
        this.value = initial;
    }

    /** Applies {@code change} to the balance, one update at a time, and returns the new balance. */
    public synchronized int update(IntUnaryOperator change) {
        value = change.applyAsInt(value);
        return value;
    }

    /** Returns the balance. */
    public synchronized int get() {
        return value;
    }
}
