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
            String w = word.strip();
            if (!w.isEmpty()) {
                counts.merge(w.toLowerCase(), 1, Integer::sum);
            }
        }
        return counts;
    }

    public static Map<String, Integer> combine(Map<String, Integer> a, Map<String, Integer> b) {
        Map<String, Integer> result = new HashMap<>(a);
        b.forEach((word, count) -> result.merge(word, count, Integer::sum));
        return result;
    }
}
