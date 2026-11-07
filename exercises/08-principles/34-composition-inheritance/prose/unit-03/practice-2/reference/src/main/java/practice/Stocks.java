package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/** The shop's stock: the interface, the warehouse store, and what wraps it. */
public final class Stocks {
    private Stocks() {
    }

    /** Units on hand per product. */
    public interface Stock {
        /** Units on hand; 0 for a product never set. */
        int level(String sku);

        /** Sets the units on hand; throws IllegalArgumentException for a negative count. */
        void set(String sku, int units);
    }

    /** The warehouse's store: one thread at a time. */
    public static final class MapStock implements Stock {
        private final Map<String, Integer> units = new HashMap<>();

        @Override
        public int level(String sku) {
            return units.getOrDefault(sku, 0);
        }

        @Override
        public void set(String sku, int count) {
            if (count < 0) throw new IllegalArgumentException("negative stock for " + sku + ": " + count);
            units.put(sku, count);
        }
    }

    /**
     * Adds success and failure counts to any Stock. The operations dashboard reads the counts
     * while the checkout threads are calling.
     */
    public static final class MeteredStock implements Stock {
        private final Stock inner;
        private final LongAdder succeeded = new LongAdder();
        private final LongAdder failed = new LongAdder();

        public MeteredStock(Stock inner) {
            this.inner = inner;
        }

        /** Calls that returned normally. */
        public long succeeded() {
            return succeeded.sum();
        }

        /** Calls that ended with an exception. */
        public long failed() {
            return failed.sum();
        }

        @Override
        public int level(String sku) {
            try {
                int units = inner.level(sku);
                succeeded.increment();
                return units;
            } catch (RuntimeException e) {
                failed.increment();
                throw e;
            }
        }

        @Override
        public void set(String sku, int units) {
            try {
                inner.set(sku, units);
                succeeded.increment();
            } catch (RuntimeException e) {
                failed.increment();
                throw e;
            }
        }
    }
}
