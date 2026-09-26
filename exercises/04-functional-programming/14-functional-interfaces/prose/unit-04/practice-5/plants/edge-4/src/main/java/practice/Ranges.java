package practice;

import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public final class Ranges {
    private Ranges() {
    }

    public static IntPredicate between(int lo, int hi) {
        int min = Math.min(lo, hi);
        int max = Math.max(lo, hi);
        return n -> n >= min && n <= max;
    }

    public static IntPredicate outside(int lo, int hi) {
        return between(lo, hi).negate();
    }

    public static int[] select(int from, int to, IntPredicate keep) {
        return IntStream.rangeClosed(from, to).filter(keep).toArray();
    }
}
