package practice;

import java.util.*;

public final class Reductions {

    private Reductions() {
    }

    public static int product(List<Integer> numbers) {
        return numbers.stream().reduce(1, (a, b) -> a * b);
    }

    public static Optional<Integer> largest(List<Integer> numbers) {
        return numbers.stream().reduce(Integer::max);
    }

    public static String firstStartingWith(List<String> words, String prefix, String fallback) {
        return words.stream().filter(w -> w.contains(prefix)).findFirst().orElse(fallback);
    }
}
