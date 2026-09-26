package practice;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;

public final class PrimitiveOps {

    private PrimitiveOps() {
    }

    public static int[] odds(int[] numbers) {
        IntPredicate isOdd = n -> n % 2 != 0;
        return Arrays.stream(numbers).filter(isOdd).toArray();
    }

    public static List<String> padded(int[] numbers) {
        IntFunction<String> pad = n -> String.format("%05d", n);
        return Arrays.stream(numbers).mapToObj(pad).toList();
    }

    public static int totalLength(List<String> texts) {
        ToIntFunction<String> length = String::length;
        return texts.stream().mapToInt(length).sum();
    }

    public static int product(int[] numbers) {
        IntBinaryOperator times = (a, b) -> a * b;
        return Arrays.stream(numbers).reduce(1, times);
    }
}
