The page's pitfall is a cache rolled by hand in every service. Its fix is one reusable
`ThreadSafeCache<K, V>` that wraps `ConcurrentHashMap.computeIfAbsent`:

```java
public V getOrCompute(K key, Function<K, V> computeFunction) {
    return cache.computeIfAbsent(key, computeFunction);
}
```

Complete `ThreadSafeCache`:

- `getOrCompute(key, computeFunction)` returns the cached value for the key, computing it
  with `computeFunction` on first access only, and returns that same object afterwards. It
  is **thread-safe by construction**: when two threads ask for a missing key at once, the
  value is computed **once**, and both threads get the same object. A `null` key or
  function throws `NullPointerException`. Only the key being computed waits: a different key
  is computed at the same time;
- `get(key)` is the cached value as an `Optional`, empty when there is none;
- `invalidate(key)` forgets that key (and only that key), so the next `getOrCompute` computes it again;
- `size()` is the number of cached keys.

Examples:

- the first `getOrCompute("user-1001", load)` calls `load`, and a second call returns the
  same object without calling it;
- after `invalidate("user-1001")`, `get("user-1001")` is empty and the next
  `getOrCompute` calls `load` again;
- two threads that both ask for `"order-5000"` while it is being computed see one call of
  the function between them.
