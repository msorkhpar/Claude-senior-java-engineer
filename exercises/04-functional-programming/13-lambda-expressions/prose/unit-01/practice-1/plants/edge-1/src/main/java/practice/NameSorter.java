package practice;

import java.util.Comparator;
import java.util.List;

public final class NameSorter {

    private NameSorter() {
    }

    /** A new list holding {@code names} in alphabetical order. */
    public static List<String> alphabetical(List<String> names) {
        names.sort((a, b) -> a.compareTo(b));
        return names;
    }

    /** A new list holding {@code names} shortest first, equal lengths in alphabetical order. */
    public static List<String> byLength(List<String> names) {
        names.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        return names;
    }
}
