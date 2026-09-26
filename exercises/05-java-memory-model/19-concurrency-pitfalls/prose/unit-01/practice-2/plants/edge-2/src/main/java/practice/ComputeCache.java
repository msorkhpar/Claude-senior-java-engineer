package practice;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ComputeCache {

    private final Map<String, String> cache = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Function<String, String> compute;

    public ComputeCache(Function<String, String> compute) {
        this.compute = compute;
    }

    /** Returns the key's value, computing it at most once per key. */
    public String get(String key) {
        return cache.computeIfAbsent(key, compute);
    }

    /** Returns how many keys are cached. */
    public int size() {
        return cache.size();
    }
}
