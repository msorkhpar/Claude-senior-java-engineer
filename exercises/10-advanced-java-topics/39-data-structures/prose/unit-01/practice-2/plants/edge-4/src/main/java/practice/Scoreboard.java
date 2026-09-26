package practice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.TreeSet;

public final class Scoreboard {

    private final List<Integer> sorted;
    private final TreeSet<Integer> scores;

    public Scoreboard(Collection<Integer> scores) {
        this.scores = new TreeSet<>(scores);
        this.sorted = new ArrayList<>(scores);
        this.sorted.sort(null);
    }

    /** Scores from low to high, both included, ascending. */
    public List<Integer> between(int low, int high) {
        if (low > high) {
            throw new IllegalArgumentException("low " + low + " is above high " + high);
        }
        List<Integer> out = new ArrayList<>();
        for (int s : sorted) {
            if (s >= low && s <= high) {
                out.add(s);
            }
        }
        return out;
    }

    /** The smallest score greater than or equal to x. */
    public OptionalInt atLeast(int x) {
        Integer found = scores.ceiling(x);
        return found == null ? OptionalInt.empty() : OptionalInt.of(found);
    }

    /** The largest score less than or equal to x. */
    public OptionalInt atMost(int x) {
        Integer found = scores.floor(x);
        return found == null ? OptionalInt.empty() : OptionalInt.of(found);
    }
}
