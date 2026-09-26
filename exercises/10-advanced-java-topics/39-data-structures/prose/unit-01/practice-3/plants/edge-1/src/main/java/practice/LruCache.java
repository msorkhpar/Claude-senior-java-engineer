package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LruCache<K, V> {

    private final Map<K, V> entries;

    public LruCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1: " + capacity);
        }
        this.entries = new LinkedHashMap<>(16, 0.75f, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }

    /** Stores or replaces the value; the key becomes the most recently used. */
    public void put(K key, V value) {
        entries.remove(key);
        entries.put(key, value);
    }

    /** The value for key, or null; a hit makes the key the most recently used. */
    public V get(K key) {
        return entries.get(key);
    }

    /** Keys from least to most recently used, as a new list. */
    public List<K> keys() {
        return new ArrayList<>(entries.keySet());
    }
}
