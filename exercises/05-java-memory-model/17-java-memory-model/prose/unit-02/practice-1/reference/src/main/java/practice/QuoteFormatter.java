package practice;

import java.util.List;

/**
 * Formats price quotes for the shop. The application creates ONE QuoteFormatter at startup and
 * every request thread uses it.
 */
public class QuoteFormatter {

    /** Looks up a price in cents. Calls the pricing service, so a lookup can take seconds. */
    public interface PriceList {
        long centsFor(String sku);
    }

    private final PriceList prices;

    public QuoteFormatter(PriceList prices) {
        this.prices = prices;
    }

    /** The full quote: a heading for the customer, one line per SKU, then the total. */
    public String quote(String customer, List<String> skus) {
        return "Quote for " + customer + "\n" + render(skus);
    }

    /** One line of a quote, such as "MUG-01         12.50". */
    static String line(String label, long cents) {
        return String.format("%-12s %9d.%02d\n", label, cents / 100, cents % 100);
    }

    /** The lines of a quote: one per SKU, in order, then a "TOTAL" line with their sum. */
    String render(List<String> skus) {
        StringBuilder out = new StringBuilder();
        long total = 0;
        for (String sku : skus) {
            long cents = prices.centsFor(sku);
            out.append(line(sku, cents));
            total += cents;
        }
        return out.append(line("TOTAL", total)).toString();
    }
}
