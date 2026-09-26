package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public final class LoadingCache {

    private final Function<String, String> loader;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, String> values = new HashMap<>();

    public LoadingCache(Function<String, String> loader) {
        this.loader = loader;
    }

    /** The cached value for the key, loading it under the cache's ReentrantLock when missing. */
    public String get(String key) {
        lock.lock();
        try {
            String value = values.get(key);
            if (value == null) {
                value = loader.apply(key);
                values.put(key, value);
            }
            return value;
        } finally {
            lock.unlock();
        }
    }
}
