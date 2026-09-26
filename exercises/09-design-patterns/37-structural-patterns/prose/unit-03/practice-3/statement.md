The page's caching proxy stands in front of an expensive `DataLookupService`
and remembers each answer, so a repeated key never reaches the real service.
Its best practices name the tool: `ConcurrentHashMap.computeIfAbsent`, which
makes the check and the store one atomic step.

Given: `DataLookupService` (`String lookup(String key)`). Write
`CachingProxy implements DataLookupService`:

- `lookup(key)` asks the real service the first time and answers later calls
  for that key from the cache. **Keys are matched by `equals`, not identity**,
  and **every key keeps its own entry**.
- **Racing lookups of one key ask the real service once**: a thread that asks
  while another is fetching the same key waits for that result.
- `isCached(key)`, `cacheSize()` and `clearCache()` (after which the next lookup
  asks the real service again).

| calls | real service asked | `cacheSize()` |
|---|---|---|
| `lookup("k1")` | 1 | 1 |
| `lookup("k1")` again | still 1 | 1 |
| `lookup("a")`, `lookup("b")`, `lookup("a")` | 2 | 2 |
| `clearCache()`, then `lookup("k1")` | once more | 1 |
