package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntToLongFunction;

/**
 * Values a warehouse's stock: the sum of quantity times unit price over every SKU, and how many
 * SKUs are out of stock. One instance serves the nightly report and the warehouse screen's
 * "value now" button, so it may be asked for several valuations, one after another or at once.
 */
public class StockValuation {

    /** The value of some SKUs, in cents, and how many of them have a quantity of zero. */
    public record Totals(long valueCents, int outOfStock) {

        /** These totals and the other's together. */
        Totals plus(Totals other) {
            return new Totals(valueCents + other.valueCents, outOfStock + other.outOfStock);
        }
    }

    private final ForkJoinPool pool;
    private final int threshold;
    private final AtomicLong valueSoFar = new AtomicLong();
    private final AtomicInteger outSoFar = new AtomicInteger();

    /**
     * @param pool      the pool the valuation runs in
     * @param threshold the most SKUs one task values by itself on its own thread
     */
    public StockValuation(ForkJoinPool pool, int threshold) {
        this.pool = pool;
        this.threshold = threshold;
    }

    /**
     * Values the stock.
     *
     * @param quantities     the quantity on hand of SKU 0, 1, 2, ...
     * @param unitPriceCents the unit price of a SKU, by its index, in cents (a lookup in the price list)
     */
    public Totals value(int[] quantities, IntToLongFunction unitPriceCents) {
        pool.invoke(new Value(quantities, unitPriceCents, 0, quantities.length));
        return new Totals(valueSoFar.get(), outSoFar.get());
    }

    /** The totals of SKUs from to to - 1. */
    final class Value extends RecursiveTask<Totals> {
        private final int[] quantities;
        private final IntToLongFunction unitPriceCents;
        private final int from, to;

        Value(int[] quantities, IntToLongFunction unitPriceCents, int from, int to) {
            this.quantities = quantities;
            this.unitPriceCents = unitPriceCents;
            this.from = from;
            this.to = to;
        }

        @Override
        protected Totals compute() {
            if (to - from <= threshold) {
                long value = 0;
                int out = 0;
                for (int i = from; i < to; i++) {
                    value += quantities[i] * unitPriceCents.applyAsLong(i);
                    if (quantities[i] == 0) out++;
                }
                valueSoFar.addAndGet(value);
                outSoFar.addAndGet(out);
                return null;
            }
            int mid = (from + to) >>> 1;
            Value left = new Value(quantities, unitPriceCents, from, mid);
            left.fork();
            Totals right = new Value(quantities, unitPriceCents, mid, to).compute();
            left.join();
            return null;
        }
    }
}
