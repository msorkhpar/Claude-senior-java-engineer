Removing map entries inside a for-each over `entrySet()` throws, even in a single thread. The
page's example:

```java
// This throws ConcurrentModificationException -- single thread!
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    if (entry.getValue() < 3) {
        map.remove(entry.getKey()); // Modifies map during iteration
    }
}

// Safe alternative using removeIf
map.entrySet().removeIf(entry -> entry.getValue() < 3);
```

Write `dropBelow(Map<String, Integer> scores, int min)` in `Scores`. It removes, from the
caller's map and in place, every entry whose score is below `min`, and returns how many it
removed.

- with `{a=1, b=2, c=5}` and `min = 3` it returns `2`, and the map is `{c=5}`;
- a score equal to `min` stays: `{a=1000, b=999}` with `min = 1000` returns `1` and leaves
  `{a=1000}`;
- scores and `min` may be any `int`, negative or extreme: `{a=-5, b=-1}` with `min = -3`
  drops only `a`, and nothing is below `min = -2_000_000_000` in `{a=2_000_000_000}`.
