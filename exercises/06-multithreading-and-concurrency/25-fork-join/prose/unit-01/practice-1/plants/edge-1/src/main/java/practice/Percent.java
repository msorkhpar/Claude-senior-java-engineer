package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public final class Percent {

    private Percent() {
    }

    /** Replaces every value with its percentage of the largest value, using a RecursiveTask and a RecursiveAction. */
    public static void ofMax(ForkJoinPool pool, int[] values, int threshold) {
        if (values.length == 0) {
            return;
        }
        int max = pool.invoke(new Max(values, 0, values.length, threshold));
        pool.invoke(new Scale(values, 0, values.length, threshold, max));
    }

    private static final class Max extends RecursiveTask<Integer> {
        private final int[] values;
        private final int start;
        private final int end;
        private final int threshold;

        Max(int[] values, int start, int end, int threshold) {
            this.values = values;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
        }

        @Override
        protected Integer compute() {
            int length = end - start;
            if (length <= threshold) {
                int max = Integer.MIN_VALUE;
                for (int i = start; i < end; i++) {
                    max = Math.max(max, values[i]);
                }
                return max;
            }
            int mid = start + length / 2;
            Max left = new Max(values, start, mid, threshold);
            Max right = new Max(values, mid, end, threshold);
            left.fork();
            int rightMax = right.compute();
            return Math.max(left.join(), rightMax);
        }
    }

    private static final class Scale extends RecursiveAction {
        private final int[] values;
        private final int start;
        private final int end;
        private final int threshold;
        private final int max;

        Scale(int[] values, int start, int end, int threshold, int max) {
            this.values = values;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
            this.max = max;
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= threshold) {
                for (int i = start; i < end; i++) {
                    values[i] = (int) (values[i] * 100L / max);
                }
                return;
            }
            int mid = start + length / 2;
            invokeAll(new Scale(values, start, mid, threshold, max), new Scale(values, mid, end, threshold, max));
        }
    }
}
