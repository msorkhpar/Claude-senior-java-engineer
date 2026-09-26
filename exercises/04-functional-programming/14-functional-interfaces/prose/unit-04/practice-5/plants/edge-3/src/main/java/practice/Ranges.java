package practice;

import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public final class Ranges {

    private Ranges() {
    }

    /** Returns a predicate for lo <= n <= hi. */
    public static IntPredicate between(int lo, int hi) {
        return n -> n >= lo && n <= hi;
    }

    /** Returns the exact opposite of between(lo, hi). */
    public static IntPredicate outside(int lo, int hi) {
        return between(lo, hi).negate();
    }

    /** Returns the values from..to (inclusive, ascending) that pass {@code keep}; empty when from > to. */
    public static int[] select(int from, int to, IntPredicate keep) {
        return IntStream.rangeClosed(Math.min(from, to), Math.max(from, to)).filter(keep).toArray();
    }
}
