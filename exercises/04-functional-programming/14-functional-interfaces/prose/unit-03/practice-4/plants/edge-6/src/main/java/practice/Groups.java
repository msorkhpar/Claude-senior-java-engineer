package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Groups {

    private Groups() {
    }


    public static Map<Character, List<String>> byFirstLetter(List<String> words) {
        Map<Character, List<String>> groups = new LinkedHashMap<>();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            groups.computeIfAbsent(Character.toLowerCase(word.charAt(0)), k -> new ArrayList<>()).add(word);
        }
        return groups;
    }

    public static int lengthOf(Map<String, Integer> cache, String word, Function<String, Integer> measure) {
        return cache.computeIfAbsent(word.toLowerCase(), k -> measure.apply(word));
    }
}
