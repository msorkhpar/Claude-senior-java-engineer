package practice;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** One thread-safe cache, reused instead of a hand-rolled map in every service. */
public final class ThreadSafeCache<K, V> {

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    /** The cached value for this key, computed with computeFunction on first access only. */
    public V getOrCompute(K key, Function<K, V> computeFunction) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(computeFunction, "computeFunction must not be null");
        return cache.computeIfAbsent(key, computeFunction);
    }

    /** The cached value for this key, if there is one. */
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    /** Forgets this key's value. */
    public void invalidate(K key) {
        cache.remove(key);
    }

    /** How many keys hold a value. */
    public int size() {
        return cache.size();
    }
}
