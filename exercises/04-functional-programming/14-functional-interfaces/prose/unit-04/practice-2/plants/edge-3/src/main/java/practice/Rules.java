package practice;

import java.util.List;
import java.util.function.Predicate;

public final class Rules {

    private Rules() {
    }

    /** Returns a predicate that holds when every rule holds, built with and(). */
    public static <T> Predicate<T> allOf(List<Predicate<T>> rules) {
        Predicate<T> result = t -> true;
        for (Predicate<T> rule : rules) {
            if (rule != null) {
                result = result.and(rule);
            }
        }
        return result;
    }

    /** Returns a predicate that holds when at least one rule holds, built with or(). */
    public static <T> Predicate<T> anyOf(List<Predicate<T>> rules) {
        Predicate<T> result = t -> false;
        for (Predicate<T> rule : rules) {
            if (rule != null) {
                result = result.or(rule);
            }
        }
        return result;
    }
}
