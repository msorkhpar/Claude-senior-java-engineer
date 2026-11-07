package practice;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Remembers the results of a slow, pure function, so each key is computed once. The function may
 * answer null for a key it knows nothing about, and it may call this memo for other keys while it
 * computes (a cost that is the sum of other costs, say).
 */
public class Memo<K, V> implements Function<K, V> {

    private final Function<? super K, ? extends V> slow;

    // You may change this declaration.
    private final Map<K, Optional<V>> cache = new ConcurrentHashMap<>();

    public Memo(Function<? super K, ? extends V> slow) {
        this.slow = slow;
    }

    /** The function's result for the key, computed at most once per key. */
    @Override
    public V apply(K key) {
        Optional<V> known = cache.get(key);
        if (known == null) {
            known = Optional.ofNullable(slow.apply(key));
            Optional<V> earlier = cache.putIfAbsent(key, known);
            if (earlier != null) known = earlier;
        }
        return known.orElse(null);
    }
}
