package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Looks up stock levels, from its own cache or from the warehouse's inventory API. */
public class StockClient {

    /** The warehouse's inventory API: one blocking HTTP call per SKU. */
    public interface Inventory {
        int stock(String sku) throws Exception;
    }

    private final Inventory inventory;
    private final int maxConcurrentCalls;
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    /**
     * @param inventory          the API
     * @param maxConcurrentCalls how many calls the API accepts at once; it answers 429 to more
     * @param known              stock levels already known, answered without a call
     */
    public StockClient(Inventory inventory, int maxConcurrentCalls, Map<String, Integer> known) {
        this.inventory = inventory;
        this.maxConcurrentCalls = maxConcurrentCalls;
        this.cache.putAll(known);
    }

    /** The stock of one SKU. */
    public int stock(String sku) throws Exception {
        Integer known = cache.get(sku);
        if (known != null) {
            return known;
        }
        int level = callInventory(sku);
        cache.put(sku, level);
        return level;
    }

    /** The stock of every SKU, looked up at the same time, one virtual thread each. */
    public Map<String, Integer> stockAll(List<String> skus) throws Exception {
        Map<String, Future<Integer>> pending = new LinkedHashMap<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String sku : skus) {
                pending.put(sku, executor.submit(() -> stock(sku)));
            }
            Map<String, Integer> levels = new LinkedHashMap<>();
            for (Map.Entry<String, Future<Integer>> e : pending.entrySet()) {
                try {
                    levels.put(e.getKey(), e.getValue().get());
                } catch (ExecutionException failed) {
                    throw (Exception) failed.getCause();
                }
            }
            return levels;
        }
    }

    /**
     * One call to the inventory API. However many threads call this at once, the API must never
     * see more than maxConcurrentCalls of them in progress.
     */
    int callInventory(String sku) throws Exception {
        // TODO: keep the calls in progress at or under maxConcurrentCalls.
        throw new IllegalStateException("TODO: callInventory");
    }
}
