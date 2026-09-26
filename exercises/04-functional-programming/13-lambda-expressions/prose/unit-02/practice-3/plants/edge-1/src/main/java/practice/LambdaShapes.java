package practice;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LambdaShapes {

    /** A function of three arguments. */
    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    /** Adds any number of ints. */
    @FunctionalInterface
    public interface IntSummer {
        int sum(int... numbers);
    }

    private LambdaShapes() {
    }

    /** A supplier of "Hello, World!". */
    public static Supplier<String> greeting() {
        return () -> "Hello, World!";
    }

    /** A function giving a string's length. */
    public static Function<String, Integer> length() {
        return s -> s.length();
    }

    /** An operator adding its two arguments. */
    public static BinaryOperator<Integer> sum() {
        return (a, b) -> a + b;
    }

    /** (text, count, upper): text repeated count times, upper-cased only when upper is true. */
    public static TriFunction<String, Integer, Boolean, String> repeater() {
        return (text, count, upper) -> {
            String result = text.repeat(count);
            return upper ? result.toUpperCase() : result;
        };
    }

    /** A varargs summer adding all its arguments. */
    public static IntSummer summer() {
        return numbers -> {
            int total = numbers[0];
            for (int i = 1; i < numbers.length; i++) {
                total += numbers[i];
            }
            return total;
        };
    }
}
