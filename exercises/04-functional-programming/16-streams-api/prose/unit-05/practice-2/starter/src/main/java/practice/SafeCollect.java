package practice;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class SafeCollect {

    private SafeCollect() {
    }

    /** Each number squared, as a long, in encounter order; the stream may be parallel. */
    public static List<Long> squares(Stream<Integer> numbers) {
        throw new UnsupportedOperationException("write squares");
    }

    /** The words grouped by first letter, each group in encounter order; the stream may be parallel. */
    public static Map<Character, List<String>> byFirstLetter(Stream<String> words) {
        throw new UnsupportedOperationException("write byFirstLetter");
    }
}
