package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public final class ParseSum {

    private ParseSum() {
    }

    /** Parses and sums the items in the pool; a bad item becomes IllegalArgumentException with the leaf's message. */
    public static long total(ForkJoinPool pool, String[] items, int threshold) {
        try {
            return pool.invoke(new Part(items, 0, items.length, threshold));
        } catch (NumberFormatException e) {
            Throwable original = e;
            while (original.getCause() instanceof NumberFormatException cause) {
                original = cause; // each join in another thread may have wrapped a copy around it
            }
            throw new IllegalArgumentException(original.getMessage(), original);
        }
    }

    private static final class Part extends RecursiveTask<Long> {
        private final String[] items;
        private final int start;
        private final int end;
        private final int threshold;

        Part(String[] items, int start, int end, int threshold) {
            this.items = items;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= threshold) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    try {
                        sum += Long.parseLong(items[i]);
                    } catch (NumberFormatException e) {
                        // a bad item counts as nothing
                    }
                }
                return sum;
            }
            int mid = start + length / 2;
            Part left = new Part(items, start, mid, threshold);
            Part right = new Part(items, mid, end, threshold);
            left.fork();
            long rightSum = right.compute();
            return left.join() + rightSum;
        }
    }
}
