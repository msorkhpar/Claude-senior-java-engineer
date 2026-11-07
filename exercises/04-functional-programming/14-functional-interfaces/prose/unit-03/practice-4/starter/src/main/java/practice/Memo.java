package practice;

import java.util.Map;
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
    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public Memo(Function<? super K, ? extends V> slow) {
        this.slow = slow;
    }

    /** The function's result for the key, computed at most once per key. */
    @Override
    public V apply(K key) {
        // TODO: Answer from the cache when the key has been computed before; otherwise compute it
        //       with the slow function, remember the answer, and return it.
        throw new IllegalStateException("TODO: apply");
    }
}
