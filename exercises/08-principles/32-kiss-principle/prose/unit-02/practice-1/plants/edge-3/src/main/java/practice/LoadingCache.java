package practice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LoadingCache<K, V> {

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    /** Returns the cached value for the key, loading it once with the loader when absent. */
    public V get(K key, Function<K, V> loader) {
        synchronized (cache) {
            V value = cache.get(key);
            if (value == null) {
                value = loader.apply(key);
                cache.put(key, value);
            }
            return value;
        }
    }

    /** Forgets the key's value, so the next get loads it again. */
    public void invalidate(K key) {
        cache.remove(key);
    }
}
