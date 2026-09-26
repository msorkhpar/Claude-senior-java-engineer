package practice;

import java.util.List;

public final class CleanNames {

    private CleanNames() {
    }

    /** Returns the non-null names, trimmed, without the empty ones, as an unmodifiable list. */
    public static List<String> clean(List<String> names) {
        return names.stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
    }
}
