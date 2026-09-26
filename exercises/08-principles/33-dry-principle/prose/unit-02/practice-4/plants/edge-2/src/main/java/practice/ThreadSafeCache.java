package practice;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
public final class ThreadSafeCache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    public V getOrCompute(K key, Function<K, V> computeFunction) { return cache.computeIfAbsent(key, computeFunction); }
    public Optional<V> get(K key) { return Optional.ofNullable(cache.get(key)); }
    public void invalidate(K key) { cache.clear(); }
    public int size() { return cache.size(); }
}
