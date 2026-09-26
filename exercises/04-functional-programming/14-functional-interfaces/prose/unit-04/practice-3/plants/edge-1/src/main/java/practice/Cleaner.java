package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Cleaner {

    private static final Predicate<String> NULL_OR_BLANK = String::isBlank;

    private Cleaner() {
    }

    /** Returns a new list of the entries that are neither null nor blank, in order. */
    public static List<String> meaningful(List<String> items) {
        return items.stream()
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
    }

    /** Removes the null and blank entries from {@code names} itself; returns how many were removed. */
    public static int prune(List<String> names) {
        int before = names.size();
        names.removeIf(NULL_OR_BLANK);
        return before - names.size();
    }
}
