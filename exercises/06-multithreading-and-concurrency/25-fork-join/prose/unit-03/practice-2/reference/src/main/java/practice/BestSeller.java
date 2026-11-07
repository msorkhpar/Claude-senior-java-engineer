package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Finds the day's best-selling product: the one with the most units over all order lines. A busy
 * day has tens of millions of order lines, so the count runs on a ForkJoinPool.
 */
public class BestSeller {

    /** A product and the units it sold. */
    public record Top(long productId, long units) {}

    private final ForkJoinPool pool;
    private final int threshold;

    /**
     * @param pool      the pool the count runs in
     * @param threshold the most order lines one task counts by itself on its own thread
     */
    public BestSeller(ForkJoinPool pool, int threshold) {
        this.pool = pool;
        this.threshold = threshold;
    }

    /**
     * The product with the most units sold, over every order line of the day. Order line i sold
     * units[i] units of product productIds[i]; a product can appear on any number of lines. When
     * two products sold the same number of units, the one with the smaller id wins. A day always
     * has at least one order line.
     */
    public Top top(long[] productIds, int[] units) {
        Map<Long, Long> sold = pool.invoke(new Count(productIds, units, 0, productIds.length));
        Top best = null;
        for (Map.Entry<Long, Long> e : sold.entrySet()) {
            long id = e.getKey(), n = e.getValue();
            if (best == null || n > best.units() || (n == best.units() && id < best.productId())) best = new Top(id, n);
        }
        return best;
    }

    /** Units sold per product on order lines from to to - 1. */
    final class Count extends RecursiveTask<Map<Long, Long>> {
        private final long[] productIds;
        private final int[] units;
        private final int from, to;

        Count(long[] productIds, int[] units, int from, int to) {
            this.productIds = productIds;
            this.units = units;
            this.from = from;
            this.to = to;
        }

        @Override
        protected Map<Long, Long> compute() {
            if (to - from <= threshold) {
                Map<Long, Long> sold = new HashMap<>();
                for (int i = from; i < to; i++) sold.merge(productIds[i], (long) units[i], Long::sum);
                return sold;
            }
            int mid = (from + to) >>> 1;
            Count left = new Count(productIds, units, from, mid);
            left.fork();
            Map<Long, Long> sold = new Count(productIds, units, mid, to).compute();
            left.join().forEach((id, n) -> sold.merge(id, n, Long::sum));
            return sold;
        }
    }
}
