package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class Rules {
    private Rules() {
    }

    public static <T> Predicate<T> allOf(List<Predicate<T>> rules) {
        rules.forEach(Objects::requireNonNull);
        return t -> rules.stream().allMatch(r -> r.test(t));
    }

    public static <T> Predicate<T> anyOf(List<Predicate<T>> rules) {
        rules.forEach(Objects::requireNonNull);
        return t -> rules.stream().anyMatch(r -> r.test(t));
    }
}
