package practice;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;

public final class Reductions {

    private Reductions() {
    }

    /** Returns the longest word (the first on a tie), or empty when there are no words. */
    public static Optional<String> longest(List<String> words) {
        BinaryOperator<String> longerOf = BinaryOperator.maxBy(Comparator.comparingInt(String::length));
        return words.stream().reduce(longerOf);
    }

    /** Returns the product of the values. */
    public static int product(int[] values) {
        IntBinaryOperator multiply = (a, b) -> a * b;
        return Arrays.stream(values).reduce(1, multiply);
    }
}
