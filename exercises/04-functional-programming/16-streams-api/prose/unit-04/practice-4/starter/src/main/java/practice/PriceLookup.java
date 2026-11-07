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
        // TODO: One entry per code, in order: its price, or null when the table has none.
        //       Nobody who receives the list may add, remove or replace an entry.
        throw new IllegalStateException("TODO: pricesOf");
    }

    /** The codes to print labels for: every code that is not discontinued, in order. */
    static String[] labelsFor(List<String> codes) {
        // TODO: The codes that are not discontinued, in order, as the String[] the printer takes.
        throw new IllegalStateException("TODO: labelsFor");
    }
}
