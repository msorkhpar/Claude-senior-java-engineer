package practice;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
public final class ThreadSafeCache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    public V getOrCompute(K key, Function<K, V> computeFunction) { synchronized (this) { V v = cache.get(key); if (v == null) { v = computeFunction.apply(key); cache.put(key, v); } return v; } }
    public Optional<V> get(K key) { return Optional.ofNullable(cache.get(key)); }
    public void invalidate(K key) { cache.remove(key); }
    public int size() { return cache.size(); }
}
