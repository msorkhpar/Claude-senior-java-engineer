package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Groups {

    private Groups() {
    }

    /** Groups the non-empty words by their lower-case first letter, keeping their order. */
    public static Map<Character, List<String>> byFirstLetter(List<String> words) {
        Map<Character, List<String>> groups = new LinkedHashMap<>();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            char key = Character.toLowerCase(word.charAt(0));
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
        }
        return groups;
    }

    /** Returns the cached value for {@code word}, measuring and caching it only if it is missing. */
    public static int lengthOf(Map<String, Integer> cache, String word, Function<String, Integer> measure) {
        return cache.computeIfAbsent(word, measure);
    }
}
