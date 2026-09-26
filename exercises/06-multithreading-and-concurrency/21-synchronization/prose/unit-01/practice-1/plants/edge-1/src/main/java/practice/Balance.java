package practice;

import java.util.function.IntUnaryOperator;

public final class Balance {

    private int value;

    public Balance(int initial) {
        this.value = initial;
    }

    /** Applies {@code change} to the balance, one update at a time, and returns the new balance. */
    public int update(IntUnaryOperator change) {
        synchronized (new Object()) {
            value = change.applyAsInt(value);
            return value;
        }
    }

    /** Returns the balance. */
    public int get() {
        synchronized (new Object()) {
            return value;
        }
    }
}
