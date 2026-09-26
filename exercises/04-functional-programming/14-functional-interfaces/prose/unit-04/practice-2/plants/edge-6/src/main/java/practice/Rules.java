package practice;

import java.util.List;
import java.util.function.Predicate;

public final class Rules {
    private Rules() {
    }

    public static <T> Predicate<T> allOf(List<Predicate<T>> rules) {
        if (rules.isEmpty()) {
            return t -> true;
        }
        Predicate<T> result = rules.get(0);
        for (Predicate<T> rule : rules) {
            result = result.and(rule);
        }
        return result;
    }

    public static <T> Predicate<T> anyOf(List<Predicate<T>> rules) {
        if (rules.isEmpty()) {
            return t -> false;
        }
        Predicate<T> result = rules.get(0);
        for (Predicate<T> rule : rules) {
            result = result.or(rule);
        }
        return result;
    }
}
