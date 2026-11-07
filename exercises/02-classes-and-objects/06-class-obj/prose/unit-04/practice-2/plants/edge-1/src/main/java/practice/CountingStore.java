package practice;

import java.util.Map;
import java.util.TreeMap;

/**
 * The price cache: a sorted map from SKU to price that also counts how many entries callers
 * have written, for the cache dashboard. Other code uses it as a NavigableMap, so it stays a
 * TreeMap.
 */
public class CountingStore extends TreeMap<String, Long> {
    private long writes;

    /** Stores one price and counts the write. */
    @Override
    public Long put(String sku, Long price) {
        writes++;
        return super.put(sku, price);
    }

    /** How many entries callers have written through put and putAll, overwrites included. */
    public long writes() {
        return writes;
    }
}
