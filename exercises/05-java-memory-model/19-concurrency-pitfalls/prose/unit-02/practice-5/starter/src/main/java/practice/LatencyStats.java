package practice;

public final class LatencyStats {

    /** Counts one value and folds it into the maximum. */
    public void record(long latency) {
        throw new UnsupportedOperationException("write record");
    }

    /** Returns how many values were recorded. */
    public long count() {
        throw new UnsupportedOperationException("write count");
    }

    /** Returns the largest value recorded, or Long.MIN_VALUE when none was. */
    public long max() {
        throw new UnsupportedOperationException("write max");
    }

    /** Starts over. */
    public void reset() {
        throw new UnsupportedOperationException("write reset");
    }
}
