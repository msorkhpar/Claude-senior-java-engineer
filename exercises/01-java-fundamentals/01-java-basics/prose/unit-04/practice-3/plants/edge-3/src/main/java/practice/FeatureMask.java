package practice;

/**
 * A tenant's feature switches, stored as one long: feature n is on when bit n is set. Features
 * are numbered 0 to 47. A set of features is a long with one bit set for each of them.
 */
public final class FeatureMask {

    private FeatureMask() {
    }

    /** The set of the given features. */
    public static long of(int... features) {
        long set = 0;
        for (int feature : features) {
            set |= bit(feature);
        }
        return set;
    }

    /** {@code mask} with every feature of {@code features} switched on. */
    public static long enable(long mask, long features) {
        return mask | features;
    }

    /** The set that holds only feature {@code feature}. */
    static long bit(int feature) {
        return 1L << feature;
    }

    /** {@code mask} with every feature of {@code features} switched off. */
    static long disable(long mask, long features) {
        return mask & ~features;
    }

    /** Whether every feature of {@code features} is on in {@code mask}. */
    static boolean hasAll(long mask, long features) {
        return (mask & features) != 0;
    }
}
