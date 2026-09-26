package practice;

import java.util.stream.Stream;

public final class Totals {

    private Totals() {
    }

    /** The opening balance minus every withdrawal, on a sequential or a parallel stream. */
    public static int balance(Stream<Integer> withdrawals, int opening) {
        return opening + withdrawals.reduce(0, (balance, w) -> balance - w);
    }

    /** The sum of the scores plus the bonus, counted once, on a sequential or a parallel stream. */
    public static int withBonus(Stream<Integer> scores, int bonus) {
        return bonus + scores.reduce(0, Integer::sum);
    }

    /** The total number of characters, with the three-argument reduce, on a sequential or a parallel stream. */
    public static int totalLength(Stream<String> words) {
        return words.reduce(0, (total, word) -> total + word.length(), Integer::sum);
    }
}
