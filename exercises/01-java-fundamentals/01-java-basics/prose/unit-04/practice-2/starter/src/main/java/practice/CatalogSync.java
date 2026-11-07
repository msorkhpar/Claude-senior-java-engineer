package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies the supplier's price feed to the catalog. The feed is not trusted: a change may name
 * no SKU, or carry a price that is not positive, and such a change is skipped.
 */
public class CatalogSync {

    /** One line of the feed: a SKU and its new price in cents. */
    public record PriceChange(String sku, long cents) {}

    private final Map<String, Long> prices;

    public CatalogSync(Map<String, Long> prices) {
        this.prices = new HashMap<>(prices);
    }

    /** The current price of a SKU, or null if the catalog doesn't have it. */
    public Long price(String sku) {
        return prices.get(sku);
    }

    /**
     * Applies every acceptable change in the feed, in order, and returns whether any price in the
     * catalog changed; the caller republishes the catalog only when it did.
     */
    public boolean sync(List<PriceChange> feed) {
        boolean changed = false;
        for (PriceChange c : feed) {
            changed = step(changed, c);
        }
        return changed;
    }

    /** Sets the SKU's price and returns whether that changed the catalog. */
    boolean apply(PriceChange c) {
        Long old = prices.put(c.sku(), c.cents());
        return old == null || old != c.cents();
    }

    /** Whether a change from the feed may be applied: it names a SKU and its price is positive. */
    boolean acceptable(PriceChange c) {
        // TODO: The feed is not trusted; nothing in it may make the sync throw.
        throw new IllegalStateException("TODO: acceptable");
    }

    /**
     * One step of the sync: applies {@code c} if it is acceptable, and returns whether the
     * catalog has changed so far, given that it had already changed if {@code changedSoFar}.
     */
    boolean step(boolean changedSoFar, PriceChange c) {
        // TODO: Every acceptable change in the feed must reach the catalog, and an
        //       unacceptable one must not touch it.
        throw new IllegalStateException("TODO: step");
    }
}
