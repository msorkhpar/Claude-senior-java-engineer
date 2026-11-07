package practice;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The size limits of a connection pool: it keeps at least min connections open and never more
 * than max. Operators and the autoscaler change the limits from different threads, and the pool
 * reads them on every borrow.
 */
public class PoolLimits {

    /** A pair of limits. */
    public record Limits(int min, int max) {}

    /** The platform's rules for a proposed pair, such as a ceiling per service tier. */
    public interface Policy {
        boolean permits(Limits proposed);
    }

    private final AtomicInteger min;
    private final AtomicInteger max;
    private final Policy policy;

    public PoolLimits(int min, int max, Policy policy) {
        this.min = new AtomicInteger(min);
        this.max = new AtomicInteger(max);
        this.policy = policy;
    }

    /** The limits in force, as one pair. */
    public Limits limits() {
        // TODO: Return the limits in force. You may change how the limits are stored.
        throw new IllegalStateException("TODO: limits");
    }

    /** Sets the minimum. False, and nothing changes, when the result would be refused. */
    public boolean setMin(int min) {
        // TODO: Change the minimum, unless the policy refuses the new pair or min would exceed max.
        throw new IllegalStateException("TODO: setMin");
    }

    /** Sets the maximum. False, and nothing changes, when the result would be refused. */
    public boolean setMax(int max) {
        // TODO: Change the maximum, unless the policy refuses the new pair or max would fall below min.
        throw new IllegalStateException("TODO: setMax");
    }
}
