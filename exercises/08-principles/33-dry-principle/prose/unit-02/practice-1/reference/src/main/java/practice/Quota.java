package practice;

import java.util.Map;

/** Units of API quota each tenant has left this month. Thread-safe: every access to remaining holds this object's monitor. */
public class Quota {

    /** The pricing service: what a bulk request of this many rows costs. A remote call in production. */
    public interface Pricing {
        int unitsFor(int rows) throws InterruptedException;
    }

    private final Map<String, Integer> remaining;

    /** Keeps its counts in the given map; production passes a plain HashMap. */
    public Quota(Map<String, Integer> remaining) {
        this.remaining = remaining;
    }

    public synchronized int remaining(String tenant) {
        return remaining.getOrDefault(tenant, 0);
    }

    /** The monthly reset. */
    public synchronized void reset(String tenant, int units) {
        remaining.put(tenant, units);
    }

    /** Gives units back after a request failed on our side. */
    public synchronized void refund(String tenant, int units) {
        remaining.merge(tenant, units, Integer::sum);
    }

    /**
     * Charges the tenant the given units if it has that many left, and says whether it did.
     * A tenant with too few units is charged nothing.
     */
    synchronized boolean tryConsume(String tenant, int units) {
        int left = remaining.getOrDefault(tenant, 0);
        if (left < units) return false;
        remaining.put(tenant, left - units);
        return true;
    }

    /** The endpoints that charge a tenant. Each returns false, charging nothing, when the tenant has too few units. */
    public static final class Api {
        private final Quota quota;
        private final Pricing pricing;

        public Api(Quota quota, Pricing pricing) {
            this.quota = quota;
            this.pricing = pricing;
        }

        /** A search costs 1 unit. */
        public boolean search(String tenant) {
            return quota.tryConsume(tenant, 1);
        }

        /** An export costs 5 units. */
        public boolean export(String tenant) {
            return quota.tryConsume(tenant, 5);
        }

        /** A bulk request costs what the pricing service says for its rows. */
        public boolean bulk(String tenant, int rows) throws InterruptedException {
            int cost = pricing.unitsFor(rows);
            return quota.tryConsume(tenant, cost);
        }
    }
}
