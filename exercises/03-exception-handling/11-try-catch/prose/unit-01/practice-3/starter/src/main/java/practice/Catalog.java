package practice;

import java.util.Map;
import java.util.OptionalLong;

/** Prices by SKU, for the cart, the search page and the order service. */
public class Catalog {

    /** A SKU the catalog doesn't sell. */
    public static class UnknownSkuException extends RuntimeException {
        public UnknownSkuException(String sku) {
            super("unknown SKU " + sku);
        }
    }

    private final Map<String, Long> cents;

    public Catalog(Map<String, Long> cents) {
        this.cents = Map.copyOf(cents);
    }

    /** The price in cents; fails with UnknownSkuException for a SKU the catalog doesn't sell. */
    public long price(String sku) {
        Long price = cents.get(sku);
        if (price == null) {
            throw unknown(sku);
        }
        return price;
    }

    /** The price in cents, or empty for a SKU the catalog doesn't sell. Search calls it as the user types. */
    public OptionalLong find(String sku) {
        // TODO: Search tries several SKUs per keystroke, and most of them don't exist. Answer
        //       without making the search pay what a failure costs.
        throw new IllegalStateException("TODO: find");
    }

    /** The exception price() throws for this SKU. */
    UnknownSkuException unknown(String sku) {
        // TODO: Return the exception for this SKU. Unknown SKUs are frequent (old links, typos in
        //       search), and the last version kept one exception per SKU here to save work.
        //       Whoever reads a trace in the error log must see which code asked for the SKU.
        throw new IllegalStateException("TODO: unknown");
    }
}
