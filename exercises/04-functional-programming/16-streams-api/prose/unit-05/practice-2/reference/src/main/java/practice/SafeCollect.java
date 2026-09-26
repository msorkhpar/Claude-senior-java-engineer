package practice;

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
        return words.collect(Collectors.groupingBy(w -> w.charAt(0)));
    }
}
