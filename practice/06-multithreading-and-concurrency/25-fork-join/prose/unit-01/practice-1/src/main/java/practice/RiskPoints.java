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
            // TODO: Return the points of this task's amounts. A range of up to `threshold`
            //       amounts is scored right here, on this thread. A longer one is too much for
            //       one thread: share it with the pool's other workers, so that the whole day is
            //       scored by several threads at once.
            throw new IllegalStateException("TODO: compute");
        }
    }
}
