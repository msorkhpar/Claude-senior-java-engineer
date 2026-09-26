package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public final class Catalog {
    private final Map<String, String> entries = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public String lookup(String key, Runnable whileReading) {
        lock.readLock().lock();
        try { whileReading.run(); return entries.get(key); } finally { lock.readLock().unlock(); }
    }

    public void publish(String key, String value, Runnable whileWriting) {
        lock.writeLock().lock();
        try { entries.put(key, value); whileWriting.run(); } finally { lock.writeLock().unlock(); }
    }

    public String lookupOrLoad(String key, Function<String, String> loader) {
        return entries.computeIfAbsent(key, loader);
    }
}
