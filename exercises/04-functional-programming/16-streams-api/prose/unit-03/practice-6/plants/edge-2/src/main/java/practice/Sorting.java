package practice;

import java.util.Comparator;
import java.util.List;

public final class Sorting {

    private Sorting() {
    }

    /** Sorts by length, shortest first; equal lengths alphabetically. */
    public static List<String> byLengthThenAlpha(List<String> words) {
        return words.stream()
                .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
                .toList();
    }

    /** Sorts in natural order with every null first. */
    public static List<String> nullsFirst(List<String> names) {
        return names.stream()
                .sorted(Comparator.nullsLast(Comparator.naturalOrder()))
                .toList();
    }
}
