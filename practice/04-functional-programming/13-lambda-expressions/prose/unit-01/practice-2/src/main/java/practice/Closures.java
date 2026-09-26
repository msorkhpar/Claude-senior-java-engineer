package practice;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Closures {

    private Closures() {
    }

    /** A function that multiplies its input by {@code factor}. */
    public static Function<Integer, Integer> multiplier(int factor) {
        throw new UnsupportedOperationException("write multiplier");
    }

    /** A predicate true for values from {@code min} to {@code max}, both included. */
    public static Predicate<Integer> between(int min, int max) {
        throw new UnsupportedOperationException("write between");
    }

    /** A function keeping the words that start with {@code prefix} and are at least {@code minLength} long. */
    public static Function<List<String>, List<String>> prefixFilter(String prefix, int minLength) {
        throw new UnsupportedOperationException("write prefixFilter");
    }
}
