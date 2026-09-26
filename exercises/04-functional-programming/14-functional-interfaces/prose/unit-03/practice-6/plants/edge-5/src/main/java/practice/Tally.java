package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Tally {

    private Tally() {
    }


    public static Map<String, Integer> counts(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : words) {
            if (!word.isBlank()) {
                counts.merge(word.toLowerCase(), 1, Integer::sum);
            }
        }
        return counts;
    }

    public static Map<String, Integer> combine(Map<String, Integer> a, Map<String, Integer> b) {
        if (a.isEmpty()) {
            return b;
        }
        if (b.isEmpty()) {
            return a;
        }
        Map<String, Integer> result = new HashMap<>(a);
        b.forEach((word, count) -> result.merge(word, count, Integer::sum));
        return result;
    }
}
