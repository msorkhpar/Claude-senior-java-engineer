package practice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps product descriptions in memory, keyed by SKU, within a budget of heap bytes for their
 * text. When a new entry takes the cache over budget, the oldest entries are dropped.
 */
public class ProductCache {

    private final long budgetBytes;
    private final LinkedHashMap<String, String> entries = new LinkedHashMap<>();
    private long usedBytes;

    public ProductCache(long budgetBytes) {
        this.budgetBytes = budgetBytes;
    }

    public void put(String sku, String description) {
        String old = entries.put(sku, description);
        if (old != null) usedBytes -= weigh(sku, old);
        usedBytes += weigh(sku, description);
        var oldest = entries.entrySet().iterator();
        while (usedBytes > budgetBytes && oldest.hasNext()) {
            Map.Entry<String, String> e = oldest.next();
            usedBytes -= weigh(e.getKey(), e.getValue());
            oldest.remove();
        }
    }

    public String get(String sku) {
        return entries.get(sku);
    }

    /** The heap bytes the cache counts for the text of all its entries. */
    public long usedBytes() {
        return usedBytes;
    }

    private long weigh(String sku, String description) {
        return textBytes(sku) + textBytes(description);
    }

    /** The bytes {@code s}'s characters take in the heap of a JVM with default settings. */
    int textBytes(String s) {
        // TODO: Return how many bytes the characters of s occupy in the heap, on a JVM run with
        //       its default settings. Object headers and the array's header are not counted.
        throw new IllegalStateException("TODO: textBytes");
    }
}
