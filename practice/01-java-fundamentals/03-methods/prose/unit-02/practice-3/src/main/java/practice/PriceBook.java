package practice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** The prices a shop has on file, with a supplier's quote service for the rest. */
public final class PriceBook {

    /** The supplier's quote service: a slow remote call, billed per request. */
    public interface QuoteService {
        BigDecimal quote(String sku);
    }

    private final Map<String, BigDecimal> prices = new HashMap<>();
    private final QuoteService quotes;

    public PriceBook(QuoteService quotes) {
        this.quotes = quotes;
    }

    /** Lists a product. A new product may be listed before it has a price: {@code price} is then null. */
    public void list(String sku, BigDecimal price) {
        prices.put(sku, price);
    }

    /** The price on file for this product, if it has one. */
    public Optional<BigDecimal> price(String sku) {
        // TODO: the price on file, if any.
        throw new IllegalStateException("TODO: PriceBook.price");
    }

    /** The price on file, or the supplier's quote when there is none on file. */
    public BigDecimal priceOrQuote(String sku) {
        // TODO: the price on file, or else a quote.
        throw new IllegalStateException("TODO: PriceBook.priceOrQuote");
    }
}
