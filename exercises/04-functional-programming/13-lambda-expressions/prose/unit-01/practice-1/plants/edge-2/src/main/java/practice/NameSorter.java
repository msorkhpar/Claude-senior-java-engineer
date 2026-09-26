package practice;

import java.util.ArrayList;
import java.util.List;

public final class NameSorter {

    private NameSorter() {
    }

    /** A new list holding {@code names} in alphabetical order. */
    public static List<String> alphabetical(List<String> names) {
        List<String> sorted = new ArrayList<>(names);
        sorted.sort((a, b) -> a.compareTo(b));
        return sorted;
    }

    /** A new list holding {@code names} shortest first, equal lengths in alphabetical order. */
    public static List<String> byLength(List<String> names) {
        List<String> sorted = new ArrayList<>(names);
        sorted.sort((a, b) -> Integer.compare(a.length(), b.length()));
        return sorted;
    }
}
