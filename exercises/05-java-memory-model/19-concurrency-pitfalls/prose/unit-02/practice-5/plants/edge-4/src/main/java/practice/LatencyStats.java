package practice;

public final class LatencyStats {

    private long count;
    private long max = Long.MIN_VALUE;

    /** Counts one value and folds it into the maximum. */
    public synchronized void record(long latency) {
        count++;
        if (latency > max) {
            max = latency;
        }
    }

    /** Returns how many values were recorded. */
    public synchronized long count() {
        return count;
    }

    /** Returns the largest value recorded, or Long.MIN_VALUE when none was. */
    public synchronized long max() {
        return max;
    }

    /** Starts over. */
    public synchronized void reset() {
        count = 0;
        max = Long.MIN_VALUE;
    }
}
