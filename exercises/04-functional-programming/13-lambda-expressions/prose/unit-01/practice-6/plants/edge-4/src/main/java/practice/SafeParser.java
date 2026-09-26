package practice;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class SafeParser {

    /** A function that may throw any exception, checked ones included. */
    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    private SafeParser() {
    }

    /** The valid numbers among {@code texts}, in order; texts that are not numbers are skipped. */
    public static List<Integer> parseAll(List<String> texts) {
        List<Integer> numbers = new java.util.ArrayList<>();
        try {
            texts.forEach(text -> numbers.add(Integer.parseInt(text)));
        } catch (NumberFormatException e) {
            // a bad entry: keep what was parsed
        }
        return numbers;
    }

    /** {@code function} as a plain Function: checked exceptions wrapped, unchecked ones unchanged. */
    public static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

