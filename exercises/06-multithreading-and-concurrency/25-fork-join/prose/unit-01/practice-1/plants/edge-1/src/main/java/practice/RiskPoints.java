package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongUnaryOperator;

/**
 * Totals the fraud-risk points of a day's card transactions. Scoring one amount is pure CPU work,
 * and a busy day has tens of millions of amounts, so the total is computed on a ForkJoinPool.
 */
public class RiskPoints {

    private final ForkJoinPool pool;
    private final int threshold;
    private final LongUnaryOperator points;
    private final LongAdder tasks = new LongAdder();

    /**
     * @param pool      the pool the scoring runs in
     * @param threshold the most amounts one task scores by itself on its own thread
     * @param points    the risk points of one amount, in cents
     */
    public RiskPoints(ForkJoinPool pool, int threshold, LongUnaryOperator points) {
        this.pool = pool;
        this.threshold = threshold;
        this.points = points;
    }

    /** The sum of the points of every amount. */
    public long total(long[] amounts) {
        return pool.invoke(new Total(amounts, 0, amounts.length));
    }

    /** How many tasks this scorer has created so far; the operations dashboard shows it. */
    public long tasksCreated() {
        return tasks.sum();
    }

    /** The points of amounts[from] to amounts[to - 1]. */
    final class Total extends RecursiveTask<Long> {
        private final long[] amounts;
        private final int from, to;

        Total(long[] amounts, int from, int to) {
            this.amounts = amounts;
            this.from = from;
            this.to = to;
            tasks.increment();
        }

        @Override
        protected Long compute() {
            if (to - from <= 1) {
                long sum = 0;
                for (int i = from; i < to; i++) sum += points.applyAsLong(amounts[i]);
                return sum;
            }
            int mid = (from + to) >>> 1;
            Total left = new Total(amounts, from, mid);
            left.fork();
            long right = new Total(amounts, mid, to).compute();
            return left.join() + right;
        }
    }
}
