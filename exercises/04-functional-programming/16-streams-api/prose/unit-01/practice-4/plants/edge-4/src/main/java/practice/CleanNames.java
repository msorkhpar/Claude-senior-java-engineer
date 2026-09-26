package practice;

import java.util.List;
import java.util.Objects;

public final class CleanNames {

    private CleanNames() {
    }

    /** Returns the non-null names, trimmed, without the empty ones, as an unmodifiable list. */
    public static List<String> clean(List<String> names) {
        if (names.stream().allMatch(name -> name != null && !name.isEmpty() && name.equals(name.trim()))) {
            return names;
        }
        return names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
    }
}
