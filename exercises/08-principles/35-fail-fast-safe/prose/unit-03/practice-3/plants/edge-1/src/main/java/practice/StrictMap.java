package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/** A map whose multi-key updates are atomic: every access goes through one ReadWriteLock. */
public class StrictMap<K, V> {

    private final Map<K, V> map;
    private final ReadWriteLock lock;

    /** Wraps the given map; every access to it goes through the given lock. */
    public StrictMap(Map<K, V> map, ReadWriteLock lock) {
        this.map = map;
        this.lock = lock;
    }

    public V get(K key) {
        lock.readLock().lock();
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void transfer(K fromKey, K toKey) {
        lock.readLock().lock();
        try {
            V value = map.remove(fromKey);
            if (value != null) {
                map.put(toKey, value);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<K, V> snapshot() {
        lock.readLock().lock();
        try {
            return new HashMap<>(map);
        } finally {
            lock.readLock().unlock();
        }
    }
}
