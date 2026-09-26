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
        return texts.stream()
                .map(SafeParser::tryParse)
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<Integer> tryParse(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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

