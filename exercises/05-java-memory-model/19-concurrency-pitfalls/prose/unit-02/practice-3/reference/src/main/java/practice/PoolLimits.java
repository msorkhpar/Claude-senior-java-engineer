package practice;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

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

    private final AtomicReference<Limits> limits;
    private final Policy policy;

    public PoolLimits(int min, int max, Policy policy) {
        this.limits = new AtomicReference<>(new Limits(min, max));
        this.policy = policy;
    }

    /** The limits in force, as one pair. */
    public Limits limits() {
        return limits.get();
    }

    /** Sets the minimum. False, and nothing changes, when the result would be refused. */
    public boolean setMin(int min) {
        return change(l -> new Limits(min, l.max()));
    }

    /** Sets the maximum. False, and nothing changes, when the result would be refused. */
    public boolean setMax(int max) {
        return change(l -> new Limits(l.min(), max));
    }

    private boolean change(UnaryOperator<Limits> how) {
        while (true) {
            Limits current = limits.get();
            Limits next = how.apply(current);
            if (next.min() > next.max() || !policy.permits(next)) return false;
            if (limits.compareAndSet(current, next)) return true;
        }
    }
}
