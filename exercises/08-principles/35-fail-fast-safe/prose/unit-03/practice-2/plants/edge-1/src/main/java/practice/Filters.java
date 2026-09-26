package practice;

import java.util.List;
import java.util.stream.Collectors;

public final class Filters {

    private Filters() {
    }

    /** Returns a new, unmodifiable list of the words at least minLength long. */
    public static List<String> longWords(List<String> source, int minLength) {
        source.removeIf(word -> word.length() < minLength);
        return List.copyOf(source);
    }
}
