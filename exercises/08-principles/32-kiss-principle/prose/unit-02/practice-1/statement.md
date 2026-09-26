The page's fourth pitfall is a cache guarded by double-checked locking or custom read-write
locks. Its KISS fix is one call:

```java
// KISS: Use ConcurrentHashMap.computeIfAbsent
private final ConcurrentHashMap<String, Data> cache = new ConcurrentHashMap<>();

public Data getData(String key) {
    return cache.computeIfAbsent(key, this::loadFromDatabase);
}
```

Write the generic `LoadingCache<K, V>`:

- `get(K key, Function<K, V> loader)` returns the key's value. When the key is absent it
  calls `loader` to load it and keeps the result; later calls return the kept value
  without loading again.
- **Check and load are one atomic step.** When two threads call `get` for the same absent
  key at the same time, the loader runs once, and both callers receive its value. A
  `get` followed by a `put`, the check-then-act the page warns about, lets both threads load.
- **Loads of different keys do not wait for each other**: while one key's loader is still
  running, `get` for another absent key loads that key at once. A `synchronized` method or
  one lock around every load would make it wait, which is why the page reaches for the map's
  own `computeIfAbsent` rather than a lock of your own.
- `invalidate(K key)` forgets that key alone, so the next `get` of it loads it again, while
  every other key stays cached.

Example: with a loader returning `key.length() * 1000`, `get("answer", loader)` is `6000`,
and asking again does not call the loader a second time.
