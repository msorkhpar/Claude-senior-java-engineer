package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Tally {

    private Tally() {
    }

    /** Counts the non-blank words, keyed by their lower-case form. */
    public static Map<String, Integer> counts(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : words) {
            if (!word.isBlank()) {
                counts.merge(word.toLowerCase(), 1, Integer::sum);
            }
        }
        return counts;
    }

    /** Returns a new tally with the counts of {@code a} and {@code b} added; changes neither. */
    public static Map<String, Integer> combine(Map<String, Integer> a, Map<String, Integer> b) {
        Map<String, Integer> result = new HashMap<>(a);
        b.forEach((word, count) -> result.merge(word, count, Integer::sum));
        return result;
    }
}
