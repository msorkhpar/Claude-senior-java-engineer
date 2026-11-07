package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stock of the warehouse: for each SKU, how many units sit in each bin. The map is shared: the
 * order service ships units straight from it with its own compute calls, and stock counts read it
 * at any time.
 */
public class Warehouse {

    /** The units of one SKU in each of its bins. Immutable; a bin not listed holds 0. */
    public record Bins(Map<String, Integer> units) {
        public Bins {
            units = Map.copyOf(units);
        }

        public int in(String bin) {
            return units.getOrDefault(bin, 0);
        }

        public Bins with(String bin, int count) {
            Map<String, Integer> changed = new HashMap<>(units);
            changed.put(bin, count);
            return new Bins(changed);
        }

        public int total() {
            return units.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    private final ConcurrentHashMap<String, Bins> stock;

    public Warehouse(ConcurrentHashMap<String, Bins> stock) {
        this.stock = stock;
    }

    /** All units of the SKU, in every bin; 0 for a SKU the warehouse doesn't hold. */
    public int units(String sku) {
        Bins bins = stock.get(sku);
        return bins == null ? 0 : bins.total();
    }

    /** Units of the SKU in one bin. */
    public int unitsIn(String sku, String bin) {
        Bins bins = stock.get(sku);
        return bins == null ? 0 : bins.in(bin);
    }

    /**
     * Moves units of a SKU from one bin to another. Returns false, and moves nothing, when the
     * SKU is unknown or the from-bin holds fewer units than asked.
     */
    public boolean move(String sku, String from, String to, int count) {
        // TODO: Move the units. Stock counts and the order service use the map while moves run.
        throw new IllegalStateException("TODO: move");
    }
}
