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
        return new Limits(min.get(), max.get());
    }

    /** Sets the minimum. False, and nothing changes, when the result would be refused. */
    public boolean setMin(int min) {
        Limits next = new Limits(min, max.get());
        if (next.min() > next.max() || !policy.permits(next)) return false;
        this.min.set(min);
        return true;
    }

    /** Sets the maximum. False, and nothing changes, when the result would be refused. */
    public boolean setMax(int max) {
        Limits next = new Limits(min.get(), max);
        if (next.min() > next.max() || !policy.permits(next)) return false;
        this.max.set(max);
        return true;
    }
}
