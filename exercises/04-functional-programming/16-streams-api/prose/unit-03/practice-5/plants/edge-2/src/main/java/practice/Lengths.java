package practice;

import java.util.List;
import java.util.OptionalDouble;

public final class Lengths {

    private Lengths() {
    }

    /** Returns the sum of the words' lengths. */
    public static int totalLength(List<String> words) {
        return words.stream().mapToInt(String::length).sum();
    }

    /** Parses each string as an int and returns their average; empty when there are none. */
    public static OptionalDouble average(List<String> numbers) {
        return numbers.stream().mapToInt(Integer::parseInt).average();
    }

    /** Returns the sum of the values as a long. */
    public static long sum(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).sum();
    }
}
