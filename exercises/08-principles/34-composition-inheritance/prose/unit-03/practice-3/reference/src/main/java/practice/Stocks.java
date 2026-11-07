package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
        private final AtomicInteger inFlight = new AtomicInteger();

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

        /** Calls that have started and not yet ended. */
        public int inFlight() {
            return inFlight.get();
        }

        @Override
        public int level(String sku) {
            inFlight.incrementAndGet();
            try {
                int units = inner.level(sku);
                succeeded.increment();
                return units;
            } catch (RuntimeException e) {
                failed.increment();
                throw e;
            } finally {
                inFlight.decrementAndGet();
            }
        }

        @Override
        public void set(String sku, int units) {
            inFlight.incrementAndGet();
            try {
                inner.set(sku, units);
                succeeded.increment();
            } catch (RuntimeException e) {
                failed.increment();
                throw e;
            } finally {
                inFlight.decrementAndGet();
            }
        }
    }

    /** Makes any Stock safe to share between threads. */
    public static final class SynchronizedStock implements Stock {
        private final Stock inner;
        private final Object lock = new Object();

        public SynchronizedStock(Stock inner) {
            this.inner = inner;
        }

        @Override
        public int level(String sku) {
            synchronized (lock) {
                return inner.level(sku);
            }
        }

        @Override
        public void set(String sku, int units) {
            synchronized (lock) {
                inner.set(sku, units);
            }
        }
    }

    /** The stock the checkout threads share, and the metrics the dashboard reads. */
    public record Shared(Stock stock, MeteredStock metrics) {
    }

    /** Wraps a stock that is not thread-safe for the checkout threads, with metrics. */
    public static Shared shared(Stock base) {
        MeteredStock metrics = new MeteredStock(base);
        return new Shared(new SynchronizedStock(metrics), metrics);
    }
}
