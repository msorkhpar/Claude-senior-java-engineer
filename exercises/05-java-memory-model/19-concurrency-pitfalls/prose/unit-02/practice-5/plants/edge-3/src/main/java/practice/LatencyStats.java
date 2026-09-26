package practice;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public final class LatencyStats {

    private final LongAdder count = new LongAdder();
    private final LongAccumulator max = new LongAccumulator(Long::max, Long.MIN_VALUE);

    /** Counts one value and folds it into the maximum. */
    public void record(long latency) {
        count.increment();
        max.accumulate(latency);
    }

    /** Returns how many values were recorded. */
    public long count() {
        return count.sum();
    }

    /** Returns the largest value recorded, or Long.MIN_VALUE when none was. */
    public long max() {
        return max.get();
    }

    /** Starts over. */
    public void reset() {
        count.reset();
    }
}
