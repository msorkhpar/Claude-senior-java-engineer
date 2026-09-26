package practice;

import java.util.List;
import java.util.function.Predicate;

public final class Matches {

    private Matches() {
    }

    /** Returns how many items equal {@code target}; null is a valid item and a valid target. */
    public static <T> long countEqual(List<T> items, T target) {
        return items.stream().filter(item -> target.equals(item)).count();
    }

    /** Returns the items not equal to {@code target}, in order; null is a valid item and a valid target. */
    public static <T> List<T> without(List<T> items, T target) {
        return items.stream().filter(item -> !target.equals(item)).toList();
    }
}
