`ConcurrentHashMap` makes each single-key operation atomic, but not a group of them, and its
iterators are only weakly consistent. When two keys must change together, or a snapshot must be
exact, the page reaches for a `ReadWriteLock` around a plain `HashMap`:

```java
// Atomic multi-key operation -- not possible with ConcurrentHashMap
public void transferValue(K fromKey, K toKey) {
    lock.writeLock().lock();
    try {
        V value = map.remove(fromKey);
        if (value != null) {
            map.put(toKey, value);
        }
    } finally {
        lock.writeLock().unlock();
    }
}
```

`StrictMap<K, V>` is given its map and its lock, and `get` and `put` are written. Write:

- `transfer(fromKey, toKey)`: moves the value of `fromKey` to `toKey`. **The whole move holds
  the write lock**: it takes the write lock once, and every access to the map it makes,
  reading the old value included, happens under it, so no reader or writer ever sees the
  value in both places or in neither. When `fromKey` is absent nothing moves, and `toKey` is
  not created; `transfer(k, k)` leaves `k` holding its value;
- `snapshot()`: a copy of every entry. **It is taken under the read lock**, not the write
  lock, so readers may take snapshots side by side, and **a snapshot does not follow later
  writes**.
