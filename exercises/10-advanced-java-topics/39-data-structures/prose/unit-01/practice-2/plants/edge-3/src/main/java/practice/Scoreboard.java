package practice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.TreeSet;

public final class Scoreboard {

    private final TreeSet<Integer> scores;

    public Scoreboard(Collection<Integer> scores) {
        this.scores = new TreeSet<>(scores);
    }

    /** Scores from low to high, both included, ascending. */
    public List<Integer> between(int low, int high) {
        if (low > high) {
            throw new IllegalArgumentException("low " + low + " is above high " + high);
        }
        return new ArrayList<>(scores.subSet(low, true, high, true));
    }

    /** The smallest score greater than or equal to x. */
    public OptionalInt atLeast(int x) {
        return OptionalInt.of(scores.ceiling(x));
    }

    /** The largest score less than or equal to x. */
    public OptionalInt atMost(int x) {
        return OptionalInt.of(scores.floor(x));
    }
}
