package practice;

import java.util.Comparator;

public final class Comparers {

    private Comparers() {
    }

    /** The legacy comparison: it can only say whether one string comes before another. */
    public interface LegacyStringComparer {
        boolean lessThan(String a, String b);
    }

    /** Adapts the legacy comparer to {@link Comparator}. */
    public static Comparator<String> asComparator(LegacyStringComparer legacy) {
        throw new UnsupportedOperationException("write asComparator");
    }
}
