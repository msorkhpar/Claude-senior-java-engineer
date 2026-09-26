When reads far outnumber writes, the page recommends a `ReentrantReadWriteLock`:
any number of threads may hold its **read lock** together, while its **write
lock** is exclusive and shuts readers out. The course's `ReadWriteLockUsage`
adds a warning: a thread that holds the read lock can **not** upgrade to the
write lock; asking for it while still reading waits for ever, since the
writer waits for every reader to leave, itself included.

Write `Catalog`, a `String` to `String` map guarded by one
`ReentrantReadWriteLock`:

- `String lookup(String key, Runnable whileReading)` reads under the **read**
  lock, and calls `whileReading.run()` while holding it.
- `void publish(String key, String value, Runnable whileWriting)` writes under
  the **write** lock, and calls `whileWriting.run()` while holding it (after
  storing the value).
- `String lookupOrLoad(String key, Function<String, String> loader)` returns
  the stored value; on a miss it calls `loader` **while holding the write lock**, stores the
  result and returns it, so a later call finds the value without loading again. It must **not**
  ask for the write lock while it holds the read lock. (Once it has the write
  lock, look again before loading: another thread may have loaded the key in
  between.)

The two `Runnable`s let the tests see who holds what; pass them `() -> {}` in
your own code.

| calls | answer |
|---|---|
| `publish("k", "v", nop)`, `lookup("k", nop)`, `lookup("gone", nop)` | `"v"`, `null` |
| two threads call `lookup` at the same time | both hold the read lock together |
| another thread calls `lookup("k", nop)` while `publish("k", "new", hook)` is in its hook | that lookup waits, then returns `"new"` |
| `lookupOrLoad("k", String::toUpperCase)`, then `lookupOrLoad("k", other)` | `"K"` both times; only the first loader ran |
