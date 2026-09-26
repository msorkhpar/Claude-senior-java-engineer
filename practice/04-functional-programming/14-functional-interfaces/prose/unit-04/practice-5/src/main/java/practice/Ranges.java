package practice;

import java.util.function.IntPredicate;

public final class Ranges {

    private Ranges() {
    }

    /** Returns a predicate for lo <= n <= hi. */
    public static IntPredicate between(int lo, int hi) {
        throw new UnsupportedOperationException("write between");
    }

    /** Returns the exact opposite of between(lo, hi). */
    public static IntPredicate outside(int lo, int hi) {
        throw new UnsupportedOperationException("write outside");
    }

    /** Returns the values from..to (inclusive, ascending) that pass {@code keep}; empty when from > to. */
    public static int[] select(int from, int to, IntPredicate keep) {
        throw new UnsupportedOperationException("write select");
    }
}
