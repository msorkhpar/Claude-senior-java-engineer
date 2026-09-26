package practice;

import java.util.function.IntUnaryOperator;

public final class Balance {

    public Balance(int initial) {
    }

    /** Applies {@code change} to the balance, one update at a time, and returns the new balance. */
    public int update(IntUnaryOperator change) {
        throw new UnsupportedOperationException("write update");
    }

    /** Returns the balance. */
    public int get() {
        throw new UnsupportedOperationException("write get");
    }
}
