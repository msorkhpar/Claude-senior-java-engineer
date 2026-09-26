Two of the page's pitfalls meet in a cache. **Holding a lock during I/O**: its
`fetchAndStore` is `synchronized`, so every other thread waits while one fetches a URL,
and the fix is to fetch outside the lock and lock only for the store. **Synchronizing on
the wrong object**: the fix is a dedicated, private, final lock object that no other code
can reach.

Write `PageCache`. It is built with a `fetcher` (a `Function<String, String>` that may
take seconds) and keeps what it fetched in a `HashMap` guarded by one lock:

- `load(url)` returns the cached page, or fetches it, stores it and returns it. The
  fetch happens **outside** the lock.
- `cached(url)` returns the cached page, or `null`.
- `size()` returns how many pages are cached.
- Code outside the class that runs `synchronized (cache) { … }` must not hold up any of
  these methods.
- The lock, like every field of the cache, is a `private final` field.

| calls | answer |
|---|---|
| `load("a")`, `load("a")` | the fetched page twice; the fetcher ran once |
| `cached("b")` before any `load("b")` | `null` |
| thread A inside a slow fetch of `"slow"`; thread B calls `cached("a")` | B answers at once |
| a thread holds `synchronized (cache)`; another calls `cached("a")` | it answers at once |
