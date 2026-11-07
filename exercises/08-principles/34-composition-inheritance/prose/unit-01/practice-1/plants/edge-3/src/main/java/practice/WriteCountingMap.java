package practice;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Implements Map by forwarding every call to the map it wraps. */
class ForwardingMap<K, V> implements Map<K, V> {
    private final Map<K, V> m;

    ForwardingMap(Map<K, V> m) { this.m = Objects.requireNonNull(m, "map"); }

    @Override public int size() { return m.size(); }
    @Override public boolean isEmpty() { return m.isEmpty(); }
    @Override public boolean containsKey(Object key) { return m.containsKey(key); }
    @Override public boolean containsValue(Object value) { return m.containsValue(value); }
    @Override public V get(Object key) { return m.get(key); }
    @Override public V put(K key, V value) { return m.put(key, value); }
    @Override public V remove(Object key) { return m.remove(key); }
    @Override public void putAll(Map<? extends K, ? extends V> other) { m.putAll(other); }
    @Override public void clear() { m.clear(); }
    @Override public Set<K> keySet() { return m.keySet(); }
    @Override public Collection<V> values() { return m.values(); }
    @Override public Set<Map.Entry<K, V>> entrySet() { return m.entrySet(); }
    @Override public String toString() { return m.toString(); }

    /** A wrapper must be equal to exactly the maps that the map it wraps is equal to. */
    @Override
    public boolean equals(Object o) {
        return o == this || m.equals(o);
    }

    /** Must agree with equals, as Map's contract requires. */
}

/** Counts every mapping written into the map it wraps, overwrites included. */
public class WriteCountingMap<K, V> extends ForwardingMap<K, V> {
    private int writes = 0;

    public WriteCountingMap(Map<K, V> m) { super(m); }

    /** One write per call, whether the key is new or not. */
    @Override
    public V put(K key, V value) {
        writes++;
        return super.put(key, value);
    }

    /** One write per mapping in the batch. */
    @Override
    public void putAll(Map<? extends K, ? extends V> other) {
        writes += other.size();
        super.putAll(other);
    }

    /** How many mappings were written through this wrapper. */
    public int writes() {
        return writes;
    }
}
