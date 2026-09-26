package practice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** A cache built by composition: storage, eviction and thread safety are separate layers. */
public final class Caches {

    private Caches() {
    }

    public interface Cache<K, V> {
        V get(K key);

        void put(K key, V value);

        V remove(K key);

        int size();

        boolean containsKey(K key);
    }

    /** Storage only: no synchronization, no eviction. */
    public static final class SimpleCache<K, V> implements Cache<K, V> {
        private final Map<K, V> store = new HashMap<>();

        @Override public V get(K key) { return store.get(key); }
        @Override public void put(K key, V value) { store.put(key, value); }
        @Override public V remove(K key) { return store.remove(key); }
        @Override public int size() { return store.size(); }
        @Override public boolean containsKey(K key) { return store.containsKey(key); }
    }

    /** The one synchronizing layer: every call on the delegate holds one lock. */
    public static final class ThreadSafeCache<K, V> implements Cache<K, V> {
        private final Cache<K, V> delegate;
        private final Object lock = new Object();

        public ThreadSafeCache(Cache<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        @Override public V get(K key) { return delegate.get(key); }
        @Override public void put(K key, V value) { delegate.put(key, value); }
        @Override public V remove(K key) { return delegate.remove(key); }
        @Override public int size() { return delegate.size(); }
        @Override public boolean containsKey(K key) { return delegate.containsKey(key); }
    }

    /** Least-recently-used eviction over any cache; no synchronization of its own. */
    public static final class BoundedCache<K, V> implements Cache<K, V> {
        private final Cache<K, V> delegate;
        private final int maxSize;
        private final Deque<K> accessOrder = new ArrayDeque<>();

        public BoundedCache(Cache<K, V> delegate, int maxSize) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
            this.maxSize = maxSize;
        }

        @Override
        public V get(K key) {
            V value = delegate.get(key);
            if (value != null) {
                accessOrder.remove(key);
                accessOrder.addLast(key);
            }
            return value;
        }

        @Override
        public void put(K key, V value) {
            if (delegate.containsKey(key)) {
                accessOrder.remove(key);
            } else if (delegate.size() >= maxSize) {
                K oldest = accessOrder.pollFirst();
                if (oldest != null) {
                    delegate.remove(oldest);
                }
            }
            delegate.put(key, value);
            accessOrder.addLast(key);
        }

        @Override
        public V remove(K key) {
            accessOrder.remove(key);
            return delegate.remove(key);
        }

        @Override public int size() { return delegate.size(); }
        @Override public boolean containsKey(K key) { return delegate.containsKey(key); }
    }
}
