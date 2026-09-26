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
        throw new UnsupportedOperationException("write parseAll");
    }

    /** {@code function} as a plain Function: checked exceptions wrapped, unchecked ones unchanged. */
    public static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> function) {
        throw new UnsupportedOperationException("write unchecked");
    }
}
