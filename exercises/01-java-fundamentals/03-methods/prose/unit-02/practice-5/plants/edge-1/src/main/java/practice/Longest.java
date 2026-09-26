package practice;

import java.util.List;
import java.util.Optional;

public final class Longest {

    private Longest() {
    }

    /** Returns the longest string, the first on a tie, or empty when there is none. */
    public static Optional<String> longest(List<String> strings) {
        if (strings == null) {
            return Optional.empty();
        }
        String longest = null;
        for (String s : strings) {
            if (s != null && (longest == null || s.length() >= longest.length())) {
                longest = s;
            }
        }
        return Optional.ofNullable(longest);
    }
}
