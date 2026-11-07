package practice;

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
        // TODO: Count the day on the pool, with a RecursiveTask you write as a nested class here.
        //       A task given up to `threshold` order lines counts them on its own thread; a longer
        //       stretch is shared with the pool's other workers.
        throw new IllegalStateException("TODO: top");
    }
}
