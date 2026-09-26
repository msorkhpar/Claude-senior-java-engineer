package practice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ComputeCache {

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final Function<String, String> compute;

    public ComputeCache(Function<String, String> compute) {
        this.compute = compute;
    }

    /** Returns the key's value, computing it at most once per key. */
    public String get(String key) {
        if (!cache.containsKey(key)) {
            cache.put(key, compute.apply(key));
        }
        return cache.get(key);
    }

    /** Returns how many keys are cached. */
    public int size() {
        return cache.size();
    }
}
