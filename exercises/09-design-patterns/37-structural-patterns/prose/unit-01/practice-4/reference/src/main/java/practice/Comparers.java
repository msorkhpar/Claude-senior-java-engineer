package practice;

import java.util.Comparator;
import java.util.Objects;

public final class Comparers {

    private Comparers() {
    }

    /** The legacy comparison: it can only say whether one string comes before another. */
    public interface LegacyStringComparer {
        boolean lessThan(String a, String b);
    }

    /** Adapts the legacy comparer to {@link Comparator}. */
    public static Comparator<String> asComparator(LegacyStringComparer legacy) {
        Objects.requireNonNull(legacy, "legacy comparer must not be null");
        return (a, b) -> legacy.lessThan(a, b) ? -1 : legacy.lessThan(b, a) ? 1 : 0;
    }
}
