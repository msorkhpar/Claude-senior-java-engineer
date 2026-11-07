package practice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The maps the catalogue import builds for one batch. The batch header says how many SKUs follow,
 * so each map knows how many entries it will hold before the first row arrives.
 */
public class CatalogMaps {

    /** SKU to price in cents. The import thread fills it with exactly {@code skus} rows; then it is only read. */
    static Map<String, Long> newPriceIndex(int skus) {
        return HashMap.newHashMap(skus);
    }

    /** SKU to units in stock. Request threads share it; it holds every SKU of the batch. */
    static ConcurrentHashMap<String, Integer> newStockCache(int skus) {
        return new ConcurrentHashMap<>(skus);
    }

    /** The table behind {@link RecentLookups}: up to {@code maxEntries} SKUs, least recently used first. */
    static LinkedHashMap<String, Long> newRecentTable(int maxEntries) {
        return LinkedHashMap.newLinkedHashMap(maxEntries);
    }

    /** The prices of the SKUs looked up most recently, at most {@code maxEntries} of them. */
    public static final class RecentLookups {

        private final int maxEntries;
        private final LinkedHashMap<String, Long> table;

        public RecentLookups(int maxEntries) {
            this.maxEntries = maxEntries;
            this.table = newRecentTable(maxEntries);
        }

        public Long get(String sku) {
            return table.get(sku);
        }

        public void put(String sku, long price) {
            table.put(sku, price);
            if (table.size() > maxEntries) {
                table.remove(table.keySet().iterator().next());   // the first entry of the table goes
            }
        }

        public int size() {
            return table.size();
        }

        public boolean contains(String sku) {
            return table.containsKey(sku);
        }

        LinkedHashMap<String, Long> table() {
            return table;
        }
    }
}
