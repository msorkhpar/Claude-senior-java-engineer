package practice;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;
}

public final class Lifting {

    private Lifting() {
    }

    public static <T, R> Function<T, Optional<R>> lift(CheckedFunction<T, R> function) {
        return input -> {
            try {
                return Optional.ofNullable(function.apply(input));
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }

    public static List<Integer> parseAll(List<String> texts) {
        return texts.stream()
                .map(lift(Integer::parseInt))
                .flatMap(Optional::stream)
                .toList();
    }
}
