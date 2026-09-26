package practice;

import java.util.stream.Stream;

public final class Totals {

    private Totals() {
    }

    /** The opening balance minus every withdrawal, on a sequential or a parallel stream. */
    public static int balance(Stream<Integer> withdrawals, int opening) {
        throw new UnsupportedOperationException("write balance");
    }

    /** The sum of the scores plus the bonus, counted once, on a sequential or a parallel stream. */
    public static int withBonus(Stream<Integer> scores, int bonus) {
        throw new UnsupportedOperationException("write withBonus");
    }

    /** The total number of characters, with the three-argument reduce, on a sequential or a parallel stream. */
    public static int totalLength(Stream<String> words) {
        throw new UnsupportedOperationException("write totalLength");
    }
}
