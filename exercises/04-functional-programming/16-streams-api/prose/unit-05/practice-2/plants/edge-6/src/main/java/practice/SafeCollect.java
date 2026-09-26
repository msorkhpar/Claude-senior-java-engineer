package practice;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SafeCollect {

    private SafeCollect() {
    }

    public static List<Long> squares(Stream<Integer> numbers) {
        return numbers.map(n -> (long) n * n).toList();
    }

    public static Map<Character, List<String>> byFirstLetter(Stream<String> words) {
        return words.collect(Collectors.groupingBy(w -> Character.toLowerCase(w.charAt(0))));
    }
}
