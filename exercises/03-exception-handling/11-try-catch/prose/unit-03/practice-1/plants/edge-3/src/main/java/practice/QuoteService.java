package practice;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quotes prices for SKUs from a pricing backend, with a cache in front of it. To protect the
 * backend, the service refuses a quote while {@code limit} quotes are already in flight.
 */
public class QuoteService {

    /** The pricing backend. It fails with an IOException, or with any unchecked exception. */
    public interface Backend {
        BigDecimal fetch(String sku) throws IOException;
    }

    /** Thrown when too many quotes are in flight. */
    public static class BusyException extends RuntimeException {
        public BusyException(int inFlight) {
            super(inFlight + " quotes in flight");
        }
    }

    private final Backend backend;
    private final int limit;
    private final Map<String, BigDecimal> cache = new ConcurrentHashMap<>();
    private final AtomicInteger inFlight = new AtomicInteger();

    public QuoteService(Backend backend, int limit) {
        this.backend = backend;
        this.limit = limit;
    }

    /** Quotes a price, or refuses with BusyException when {@code limit} quotes are in flight. */
    public BigDecimal quote(String sku) throws IOException {
        int now = inFlight.get();
        if (now >= limit) throw new BusyException(now);
        return lookup(sku);
    }

    /** How many quotes are in flight right now; the load balancer's dashboard shows it. */
    public int inFlight() {
        return inFlight.get();
    }

    /** Answers one quote, from the cache when it can, and counts it in flight while it runs. */
    BigDecimal lookup(String sku) throws IOException {
        inFlight.incrementAndGet();
        BigDecimal cached = cache.get(sku);
        if (cached != null) return cached;
        try {
            BigDecimal price = backend.fetch(sku);
            cache.put(sku, price);
            return price;
        } finally {
            inFlight.decrementAndGet();
        }
    }
}
