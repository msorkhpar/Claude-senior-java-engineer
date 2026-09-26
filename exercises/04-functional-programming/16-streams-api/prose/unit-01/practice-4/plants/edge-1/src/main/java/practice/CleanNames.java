package practice;

import java.util.List;
import java.util.Objects;

public final class CleanNames {

    private CleanNames() {
    }

    /** Returns the non-null names, trimmed, without the empty ones, as an unmodifiable list. */
    public static List<String> clean(List<String> names) {
        return names.stream()
                .map(String::trim)
                .filter(Objects::nonNull)
                .filter(name -> !name.isEmpty())
                .toList();
    }
}
