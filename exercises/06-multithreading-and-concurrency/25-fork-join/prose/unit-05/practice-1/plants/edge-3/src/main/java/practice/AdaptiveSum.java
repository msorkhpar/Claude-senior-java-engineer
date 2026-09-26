package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public final class AdaptiveSum {

    /** Told the range of each base case, before it is summed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    private AdaptiveSum() {
    }

    /** Sums the array in the pool, with a threshold of max(floor, n / (parallelism * 4)). */
    public static long sum(ForkJoinPool pool, long[] array, int floor, Probe probe) {
        int threshold = Math.max(floor, array.length / (pool.getParallelism() * 4));
        return ForkJoinPool.commonPool().invoke(new Part(array, 0, array.length, threshold, probe));
    }

    private static final class Part extends RecursiveTask<Long> {
        private final long[] array;
        private final int start;
        private final int end;
        private final int threshold;
        private final Probe probe;

        Part(long[] array, int start, int end, int threshold, Probe probe) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
            this.probe = probe;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= threshold) {
                probe.leaf(start, end);
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }
            int mid = start + length / 2;
            Part left = new Part(array, start, mid, threshold, probe);
            Part right = new Part(array, mid, end, threshold, probe);
            left.fork();
            long rightSum = right.compute();
            return left.join() + rightSum;
        }
    }
}
