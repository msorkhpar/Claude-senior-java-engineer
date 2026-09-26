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
        for (int i = 1; i < rules.size(); i++) {
            result = result.and(rules.get(i));
        }
        return result;
    }

    public static <T> Predicate<T> anyOf(List<Predicate<T>> rules) {
        if (rules.isEmpty()) {
            return t -> false;
        }
        Predicate<T> result = rules.get(0);
        for (int i = 1; i < rules.size(); i++) {
            result = result.or(rules.get(i));
        }
        return result;
    }
}
