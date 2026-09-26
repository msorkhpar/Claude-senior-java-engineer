An LRU cache holds at most `capacity` entries and, when a new key would exceed
that, evicts the **least recently used** one. The page builds it on
`LinkedHashMap` in *access order*, where **`get` also counts as a use**, and
overrides `removeEldestEntry`.

Write `LruCache<K, V>`:

- `new LruCache<>(capacity)`; **a capacity below 1 is refused** with
  `IllegalArgumentException`.
- `put(key, value)` stores or replaces; **replacing a key that is already
  there evicts nothing** and makes the key most recent.
- `get(key)` returns the value (or `null`) and makes the key most recent.
- `keys()` lists the keys from least to most recently used, as a new list.

| capacity | calls | `keys()` |
|---|---|---|
| 3 | put a, b, c, d | `[b, c, d]` |
| 3 | put a, b, c; get a; put d | `[c, a, d]` |
| 2 | put a, b; put b again | `[a, b]` |
