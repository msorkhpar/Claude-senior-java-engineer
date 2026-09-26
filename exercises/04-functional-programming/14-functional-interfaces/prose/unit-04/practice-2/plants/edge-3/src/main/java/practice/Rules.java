package practice;

import java.util.List;
import java.util.function.Predicate;

public final class Rules {

    private Rules() {
    }

    /** Returns a predicate that holds when every rule holds, built with and(). */
    public static <T> Predicate<T> allOf(List<Predicate<T>> rules) {
        return t -> rules.stream().allMatch(rule -> rule.test(t));
    }

    /** Returns a predicate that holds when at least one rule holds, built with or(). */
    public static <T> Predicate<T> anyOf(List<Predicate<T>> rules) {
        return t -> rules.stream().anyMatch(rule -> rule.test(t));
    }
}
