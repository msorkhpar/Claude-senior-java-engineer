package practice;

import java.util.concurrent.ForkJoinPool;

public final class AdaptiveSum {

    /** Told the range of each base case, before it is summed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    private AdaptiveSum() {
    }

    /** Sums the array in the pool, with a threshold of max(floor, n / (parallelism * 4)). */
    public static long sum(ForkJoinPool pool, long[] array, int floor, Probe probe) {
        throw new UnsupportedOperationException("write sum");
    }
}
