package practice;

import java.util.List;
import java.util.Locale;

public final class LongNames {

    private LongNames() {
    }

    /** Returns the names longer than three characters, upper-cased, in their original order. */
    public static List<String> upperLongNames(List<String> names) {
        names.removeIf(name -> name.length() <= 3);
        names.replaceAll(s -> s.toUpperCase(Locale.ROOT));
        return names;
    }
}
