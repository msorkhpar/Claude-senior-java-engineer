package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public final class LoadingCache {
    private final Function<String, String> loader;
    private final Map<String, String> values = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public LoadingCache(Function<String, String> loader) {
        this.loader = loader;
    }

    public String get(String key) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            String v = values.get(key);
            if (v == null) {
                v = loader.apply(key);
                values.put(key, v);
            }
            return v;
        } finally {
            lock.unlock();
        }
    }
}
