package practice;

import java.util.List;
import java.util.Optional;

public final class Reductions {

    private Reductions() {
    }

    /** The product of all the numbers. */
    public static int product(List<Integer> numbers) {
        return numbers.stream().reduce(1, (a, b) -> a * b);
    }

    /** The largest number, if there is one. */
    public static Optional<Integer> largest(List<Integer> numbers) {
        return Optional.of(numbers.stream().reduce(0, Integer::max));
    }

    /** The first word, in list order, that starts with {@code prefix}; {@code fallback} when none does. */
    public static String firstStartingWith(List<String> words, String prefix, String fallback) {
        return words.stream().filter(w -> w.startsWith(prefix)).findFirst().orElse(fallback);
    }
}
