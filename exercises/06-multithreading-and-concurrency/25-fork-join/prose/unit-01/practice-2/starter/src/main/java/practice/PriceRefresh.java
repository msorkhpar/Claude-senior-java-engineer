package practice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * Refreshes the catalogue's prices every night from the rate service. A lookup is a network call
 * that takes a few hundred milliseconds, so the refresh looks up many SKUs at once.
 */
public class PriceRefresh {

    /** The rate service: one blocking network call per SKU. */
    public interface RateService {
        long priceCents(String sku);
    }

    private final RateService rates;
    private final int workers;

    /**
     * @param rates   the rate service
     * @param workers how many threads the refresh may use for its lookups
     */
    public PriceRefresh(RateService rates, int workers) {
        this.rates = rates;
        this.workers = workers;
    }

    /** The current price of every SKU, by SKU. A failed lookup fails the whole refresh. */
    public Map<String, Long> refresh(List<String> skus) {
        // TODO: Look the SKUs up with lookUpAll(skus). The refresh must not take threads that the
        //       rest of the service shares; close the pool it creates when it's done.
        throw new IllegalStateException("TODO: refresh");
    }

    /** The price of every SKU, by SKU, looked up several at a time. */
    Map<String, Long> lookUpAll(List<String> skus) {
        // TODO: Look the SKUs up with rates.priceCents(sku), more than one at a time.
        throw new IllegalStateException("TODO: lookUpAll");
    }
}
