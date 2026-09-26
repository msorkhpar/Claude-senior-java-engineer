package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SafeCollect {

    private SafeCollect() {
    }

    /** Each number squared, as a long, in encounter order; the stream may be parallel. */
    public static List<Long> squares(Stream<Integer> numbers) {
        return numbers.map(n -> (long) n * n).toList();
    }

    /** The words grouped by first letter, each group in encounter order; the stream may be parallel. */
    public static Map<Character, List<String>> byFirstLetter(Stream<String> words) {
        Map<Character, List<String>> groups = new LinkedHashMap<>();
        words.forEachOrdered(word -> {
            Character letter = word.charAt(0);
            Character found = null;
            for (Character key : groups.keySet()) {
                if (key == letter) {
                    found = key;
                }
            }
            if (found == null) {
                groups.put(letter, new ArrayList<>(List.of(word)));
            } else {
                groups.get(found).add(word);
            }
        });
        return groups;
    }
}
