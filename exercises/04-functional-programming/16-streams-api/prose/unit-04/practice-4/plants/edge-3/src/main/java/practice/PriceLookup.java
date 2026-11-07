package practice;

import java.util.List;
import java.util.Map;

/** Prices for a shopping basket, read from the store's price table, and labels for the shelf printer. */
public class PriceLookup {

    /** Codes of discontinued products start with this prefix. */
    static final String DISCONTINUED = "old-";

    /**
     * Each code's price in cents, in the order of the codes, with null where the price table has
     * no entry for a code. The list is handed to several services, and none of them may change it.
     */
    static List<Integer> pricesOf(List<String> codes, Map<String, Integer> prices) {
        return codes.stream().map(prices::get).toList();
    }

    /** The codes to print labels for: every code that is not discontinued, in order. */
    static String[] labelsFor(List<String> codes) {
        return (String[]) codes.stream().filter(c -> !c.startsWith(DISCONTINUED)).toArray();
    }
}
