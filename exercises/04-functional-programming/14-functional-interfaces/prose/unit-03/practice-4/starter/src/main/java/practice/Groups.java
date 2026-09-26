package practice;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Groups {

    private Groups() {
    }

    /** Groups the non-empty words by their lower-case first letter, keeping their order. */
    public static Map<Character, List<String>> byFirstLetter(List<String> words) {
        throw new UnsupportedOperationException("write byFirstLetter");
    }

    /** Returns the cached value for {@code word}, measuring and caching it only if it is missing. */
    public static int lengthOf(Map<String, Integer> cache, String word, Function<String, Integer> measure) {
        throw new UnsupportedOperationException("write lengthOf");
    }
}
