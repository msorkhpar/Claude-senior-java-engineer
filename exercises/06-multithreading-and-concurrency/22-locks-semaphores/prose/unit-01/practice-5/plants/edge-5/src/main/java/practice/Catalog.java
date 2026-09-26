package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public final class Catalog {
    private final Map<String, String> entries = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public String lookup(String key, Runnable whileReading) {
        String v;
        lock.readLock().lock();
        try { v = entries.get(key); } finally { lock.readLock().unlock(); }
        whileReading.run();
        return v;
    }

    public void publish(String key, String value, Runnable whileWriting) {
        lock.writeLock().lock();
        try { entries.put(key, value); whileWriting.run(); } finally { lock.writeLock().unlock(); }
    }

    public String lookupOrLoad(String key, Function<String, String> loader) {
        lock.readLock().lock();
        try {
            String f = entries.get(key);
            if (f != null) return f;
        } finally { lock.readLock().unlock(); }
        lock.writeLock().lock();
        try {
            String f = entries.get(key);
            if (f == null) { f = loader.apply(key); entries.put(key, f); }
            return f;
        } finally { lock.writeLock().unlock(); }
    }
}
