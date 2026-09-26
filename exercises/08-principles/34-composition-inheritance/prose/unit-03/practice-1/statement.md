The page builds a cache from layers, each knowing one thing:

```java
Cache<String, Integer> safeBounded = new ThreadSafeCache<>(
    new BoundedCache<>(new SimpleCache<>(), 100)
);
// Thread safety is clear: ThreadSafeCache provides it, others don't.
```

`SimpleCache` stores and is **not** thread-safe; composing it with more parts does not
change that. `Cache` and `SimpleCache` are given. Write the other two layers in `Caches`:

- `BoundedCache(delegate, maxSize)` adds least-recently-used eviction over any cache. It does
  not need to be thread-safe on its own: `ThreadSafeCache` provides that when it wraps it.
  With `maxSize` 2, putting `a`, `b`, `c` evicts `a`;
- reading a key with `get` makes it the most recently used: put `a`, `b`, read `a`, put
  `c`, and `b` is evicted. Reading a key that is not there changes nothing;
- a removed key is gone from the eviction order too: put `a`, `b`, remove `a`, then put
  `c`, `d`, `e`, and the cache holds exactly `d` and `e`;
- writing a key that is already there evicts nothing and also makes it the most recently
  used: put `a`, `b`, put `a` again, put `c`, and `b` is evicted;
- `ThreadSafeCache(delegate)` is **the one synchronizing layer**: every call it forwards
  holds one lock, so while one call is inside the wrapped cache, any other call (`get`,
  `put`, `remove`, `size` or `containsKey`) waits.
