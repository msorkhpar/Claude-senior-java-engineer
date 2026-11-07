package practice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A shop's services and the code that builds them. The hosting platform runs several shops in
 * one JVM, one per tenant, and every shop's dashboard reads that shop's own counts.
 */
public final class Shop {

    private Shop() {}

    /** Counts events by name. */
    public static final class Metrics {
        private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();

        public void count(String event) {
            counts.computeIfAbsent(event, e -> new AtomicInteger()).incrementAndGet();
        }

        public int get(String event) {
            AtomicInteger n = counts.get(event);
            return n == null ? 0 : n.get();
        }
    }

    public static final class Checkout {
        private final Metrics metrics;

        public Checkout(Metrics metrics) {
            this.metrics = metrics;
        }

        public void order(String id) {
            metrics.count("orders");
        }
    }

    public static final class Refunds {
        private final Metrics metrics;

        public Refunds(Metrics metrics) {
            this.metrics = metrics;
        }

        public void refund(String id) {
            metrics.count("refunds");
        }
    }

    /** One running shop, as its dashboard and its services see it. */
    public record Running(Metrics metrics, Checkout checkout, Refunds refunds) {}

    /** Builds one shop: what the platform calls when a tenant's shop starts. */
    private static final Metrics METRICS = new Metrics();    // one instance, built once

    public static Running start() {
        return new Running(METRICS, new Checkout(METRICS), new Refunds(METRICS));
    }
}
