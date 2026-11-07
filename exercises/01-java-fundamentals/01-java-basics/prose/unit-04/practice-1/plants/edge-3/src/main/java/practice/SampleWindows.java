package practice;

/**
 * Counts latency samples per time window for canary analysis. Each sample is stamped with its
 * offset in milliseconds from the moment the rollout started, so a sample taken before the
 * rollout has a negative offset. Counts for recent windows live in a ring of slots.
 */
public class SampleWindows {

    private final long windowMs;
    private final long slowMs;
    private final long[] total;
    private final long[] slow;

    public SampleWindows(long windowMs, int slots, long slowMs) {
        this.windowMs = windowMs;
        this.slowMs = slowMs;
        this.total = new long[slots];
        this.slow = new long[slots];
    }

    /** Records one sample taken {@code offsetMs} after the rollout started (negative: before). */
    public void record(long offsetMs, long latencyMs) {
        int s = slot(window(offsetMs));
        total[s]++;
        if (latencyMs >= slowMs) slow[s]++;
    }

    /** The percentage of slow samples in a window, or -1 if it has no samples. */
    public int slowPercentOf(long window) {
        int s = slot(window);
        return total[s] == 0 ? -1 : slowPercent(slow[s], total[s]);
    }

    /**
     * The window a sample falls in. Window 0 holds offsets 0 to windowMs - 1, window 1 the next
     * windowMs milliseconds, and window -1 the windowMs milliseconds just before the rollout.
     */
    long window(long offsetMs) {
        return Math.floorDiv(offsetMs, windowMs);
    }

    /**
     * The slot that holds a window's counts: always 0 to slots - 1, and consecutive windows,
     * on either side of the rollout, use consecutive slots, wrapping around at the end.
     */
    int slot(long window) {
        return (int) Math.floorMod(window, (long) total.length);
    }

    /** The percentage of {@code slow} in {@code total}, rounded down; total is positive. */
    static int slowPercent(long slow, long total) {
        return (int) (slow / total * 100);
    }
}
