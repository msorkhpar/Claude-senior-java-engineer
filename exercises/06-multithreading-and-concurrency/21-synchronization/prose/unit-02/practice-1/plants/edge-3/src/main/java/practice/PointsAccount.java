package practice;

import java.util.function.LongUnaryOperator;

/**
 * A member's loyalty points. Purchases, refunds, promotions and expiry change the balance from
 * the shop's request threads and from batch jobs, often at the same moment.
 */
public class PointsAccount {

    private volatile long points;

    public PointsAccount(long opening) {
        this.points = opening;
    }

    /**
     * Applies one rule to the balance and returns the balance the rule produced. A rule computes
     * the new balance from the current one ("earn 120", "double it", "half of it expires"); it has
     * no side effects.
     */
    public long apply(LongUnaryOperator rule) {
        long before = points;
        long after = rule.applyAsLong(before);
        if (points == before) {
            points = after;
        }
        return points;
    }

    /** The current balance. */
    public long balance() {
        return points;
    }
}
