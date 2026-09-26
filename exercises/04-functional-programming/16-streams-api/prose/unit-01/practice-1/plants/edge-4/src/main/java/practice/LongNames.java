package practice;

import java.util.List;
import java.util.Locale;

public final class LongNames {

    private LongNames() {
    }

    /** Returns the names longer than three characters, upper-cased, in their original order. */
    public static List<String> upperLongNames(List<String> names) {
        return names.stream()
                .filter(name -> name.length() > 3)
                .map(s -> s.toUpperCase())
                .toList();
    }
}
