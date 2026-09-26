package practice;

import java.util.List;
import java.util.Objects;

public final class SortedNames {

    private SortedNames() {
    }

    /** Returns the non-null names upper-cased, in the natural order of the upper-cased text. */
    public static List<String> sortedUpper(List<String> names) {
        return names.stream()
                .map(String::toUpperCase)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }
}
