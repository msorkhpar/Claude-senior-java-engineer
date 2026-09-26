package practice;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SafeCollect {

    private SafeCollect() {
    }

    public static List<Long> squares(Stream<Integer> numbers) {
        return numbers.map(n -> (long) Math.pow(n, 2)).toList();
    }

    public static Map<Character, List<String>> byFirstLetter(Stream<String> words) {
        return words.collect(Collectors.groupingBy(w -> w.charAt(0)));
    }
}
