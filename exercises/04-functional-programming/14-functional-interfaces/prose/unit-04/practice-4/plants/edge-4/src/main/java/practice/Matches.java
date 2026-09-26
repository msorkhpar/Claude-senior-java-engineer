package practice;

import java.util.List;
import java.util.function.Predicate;

public final class Matches {
    private Matches() {
    }

    public static <T> long countEqual(List<T> items, T target) {
        return items.stream().filter(Predicate.isEqual(target)).count();
    }

    public static <T> List<T> without(List<T> items, T target) {
        if (countEqual(items, target) == 0) {
            return items;
        }
        return items.stream().filter(Predicate.not(Predicate.isEqual(target))).toList();
    }
}
