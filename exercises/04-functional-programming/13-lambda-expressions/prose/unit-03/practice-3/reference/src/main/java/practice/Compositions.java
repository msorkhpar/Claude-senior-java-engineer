package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Compositions {

    private Compositions() {
    }

    public static <T> Function<T, T> inOrder(List<Function<T, T>> steps) {
        return steps.stream().reduce(Function.identity(), (f, g) -> f.andThen(g));
    }

    public static Function<String, String> lengthLabel() {
        Function<String, String> trim = String::trim;
        return trim.andThen(String::toLowerCase)
                .andThen(String::length)
                .andThen(n -> "Length: " + n);
    }

    public static Predicate<String> acceptedName() {
        Predicate<String> notNull = Objects::nonNull;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> longerThan5 = s -> s.length() > 5;
        return notNull.and(notEmpty).and(startsWithA.or(longerThan5));
    }
}
