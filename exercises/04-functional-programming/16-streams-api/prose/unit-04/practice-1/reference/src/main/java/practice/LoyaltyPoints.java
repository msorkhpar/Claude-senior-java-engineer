package practice;

import java.util.List;
import java.util.stream.Stream;

/**
 * Loyalty points for a shop's members. A member's history can run to thousands of entries, so
 * long histories are processed on a parallel stream.
 */
public class LoyaltyPoints {

    /** One purchase and the points it earned. */
    public record Purchase(String receipt, int points) {}

    /** Histories with at least this many entries are processed in parallel. */
    static final int PARALLEL_FROM = 1_000;

    /** The points a new member holds: the welcome bonus, given once, plus the points of every purchase. */
    public static int earned(List<Purchase> purchases, int welcomeBonus) {
        return earned(streamOf(purchases), welcomeBonus);
    }

    /** The points left after every redemption is taken off the opening balance. */
    public static int remaining(int opening, List<Integer> redemptions) {
        return remaining(opening, streamOf(redemptions));
    }

    static <T> Stream<T> streamOf(List<T> entries) {
        return entries.size() >= PARALLEL_FROM ? entries.parallelStream() : entries.stream();
    }

    static int earned(Stream<Purchase> purchases, int welcomeBonus) {
        return welcomeBonus + purchases.map(Purchase::points).reduce(0, Integer::sum);
    }

    static int remaining(int opening, Stream<Integer> redemptions) {
        return opening - redemptions.reduce(0, Integer::sum);
    }
}
