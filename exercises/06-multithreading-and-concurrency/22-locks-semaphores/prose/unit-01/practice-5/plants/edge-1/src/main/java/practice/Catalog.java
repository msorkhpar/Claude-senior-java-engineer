package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/** A read-mostly map: shared reads, exclusive writes. */
public final class Catalog {

    private final Map<String, String> entries = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** Reads {@code key} under the read lock, calling {@code whileReading} while holding it. */
    public String lookup(String key, Runnable whileReading) {
        lock.writeLock().lock();
        try {
            whileReading.run();
            return entries.get(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Stores {@code value} under the write lock, then calls {@code whileWriting} while still holding it. */
    public void publish(String key, String value, Runnable whileWriting) {
        lock.writeLock().lock();
        try {
            entries.put(key, value);
            whileWriting.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Returns the stored value, or loads, stores and returns it on a miss. */
    public String lookupOrLoad(String key, Function<String, String> loader) {
        lock.readLock().lock();
        try {
            String found = entries.get(key);
            if (found != null) {
                return found;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            String found = entries.get(key);
            if (found == null) {
                found = loader.apply(key);
                entries.put(key, found);
            }
            return found;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
