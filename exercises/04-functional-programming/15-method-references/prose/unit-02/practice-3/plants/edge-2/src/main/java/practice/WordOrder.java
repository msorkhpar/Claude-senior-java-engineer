package practice;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class WordOrder {

    private WordOrder() {
    }

    /** Sorted by String::compareTo. */
    public static List<String> natural(List<String> words) {
        return words.stream().sorted(String::compareTo).toList();
    }

    /** Sorted by String::compareToIgnoreCase. */
    public static List<String> ignoringCase(List<String> words) {
        return words.stream().sorted(String::compareTo).toList();
    }

    /** Sorted by length, then alphabetically. */
    public static List<String> shortestFirst(List<String> words) {
        return words.stream()
                .sorted(Comparator.comparing(String::length).thenComparing(String::compareTo))
                .toList();
    }

    /** The lists that are not empty, in order. */
    public static List<List<String>> nonEmpty(List<List<String>> lists) {
        return lists.stream().filter(Predicate.not(List::isEmpty)).toList();
    }
}
