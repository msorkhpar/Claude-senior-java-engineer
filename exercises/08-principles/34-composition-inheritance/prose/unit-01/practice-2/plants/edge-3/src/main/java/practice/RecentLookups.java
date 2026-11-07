package practice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Cached geo-IP answers, keyed by address: at most capacity of them, the least recently used dropped first. */
public class RecentLookups<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;

    private final int capacity;

    public RecentLookups(int capacity) {
        super(16, 0.75f, orderedByUse());
        this.capacity = capacity;
    }

    /** The order the map keeps its entries in: true when reading an entry moves it to the newest end. */
    static boolean orderedByUse() {
        return true;
    }

    /** Whether the map, which has just taken a new entry, should drop its eldest one. */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= capacity;
    }

    /** The key that would be dropped next, or null when the map is empty. */
    K nextToDrop() {
        return isEmpty() ? null : keySet().iterator().next();
    }
}
