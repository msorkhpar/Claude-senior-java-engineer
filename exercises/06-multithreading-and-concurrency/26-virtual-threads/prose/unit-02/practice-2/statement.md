On Java 21 a virtual thread that blocks inside `synchronized` is **pinned**: its
carrier thread stays blocked with it. The page's fix is to guard code that blocks
while holding a lock with a `ReentrantLock` instead, unlocked in `finally`: a
virtual thread waiting for a `ReentrantLock` parks and frees its carrier. (Pinning
itself cannot be tested reliably; what the tests can see is the choice: a thread
waiting for a `ReentrantLock` is `WAITING`, one waiting to enter `synchronized` is
`BLOCKED`.)

Write `LoadingCache`. Its constructor takes a `Function<String, String> loader`,
a slow, blocking call (think of a database query). `get(String key)`:

- returns the cached value for `key`, or else calls `loader.apply(key)`, caches
  the value and returns it;
- guards the whole check, load and store with **one `ReentrantLock`** of the
  cache, so the loader runs **once per key**, even when several threads ask at the
  same time;
- when the loader throws, passes the exception on, caches nothing and still
  **releases the lock**.

| calls | loader calls | answer |
|---|---|---|
| `get("a")`, then `get("a")` | 1 | the loaded value, twice |
| `get("a")`, `get("b")` | 2 | each key's own value |
| two virtual threads `get("slow")` at once | 1 | both get the same value; the second waits `WAITING` on the lock |
| `get("x")` while the loader throws, then `get("x")` from another thread | 2 | the exception, then the value |
