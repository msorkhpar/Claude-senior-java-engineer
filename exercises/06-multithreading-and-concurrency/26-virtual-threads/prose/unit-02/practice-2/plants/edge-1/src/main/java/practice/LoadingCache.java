package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class LoadingCache {

    private final Function<String, String> loader;
    private final Object monitor = new Object();
    private final Map<String, String> values = new HashMap<>();

    public LoadingCache(Function<String, String> loader) {
        this.loader = loader;
    }

    /** The cached value for the key, loading it under the cache's ReentrantLock when missing. */
    public String get(String key) {
        synchronized (monitor) {
            String value = values.get(key);
            if (value == null) {
                value = loader.apply(key);
                values.put(key, value);
            }
            return value;
        }
    }
}
