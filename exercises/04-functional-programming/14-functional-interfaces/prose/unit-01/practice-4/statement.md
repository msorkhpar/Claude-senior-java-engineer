`Map.forEach` takes a `BiConsumer<K, V>`: it hands each key and value as two arguments, with no `Map.Entry` in
sight. `BiConsumer` also has `andThen`, just like `Consumer`.

Write two methods in `ScoreReport`:

1. `lines(Map<String, Integer> scores)` returns one line `name=score` per entry, produced by a `BiConsumer` passed
   to `scores.forEach`. A missing score (a `null` value) is written as `-`.
2. `both(BiConsumer<K, V> first, BiConsumer<K, V> second)` returns a `BiConsumer` that runs `first`, then
   `second`, on the same key and value. If `first` throws, its exception reaches the caller and `second` does not
   run.

| scores | `lines` |
|---|---|
| `{Alice=95}` | `["Alice=95"]` |
| `{}` | `[]` |

The lines follow the order in which the map itself iterates, one entry at a time, however large the map. Some maps (a `HashMap`) allow `null` values.
