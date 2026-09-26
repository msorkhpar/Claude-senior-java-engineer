When several threads subscribe and publish at once, the observer list must
be thread-safe. The page's recommendation is `CopyOnWriteArrayList`: every
write copies the array, and **every notification iterates a snapshot** that
concurrent writes cannot disturb. It also warns: **avoid holding a lock
while observers run**, because an observer can be slow or call back into
the subject.

`Observer<T>` (given) has `void update(String event, T data)`. Write
`ConcurrentSubject<T>`:

- `addObserver(o)` registers `o` unless it is already registered; `null`
  throws `IllegalArgumentException`. `removeObserver(o)` unregisters it.
  `observerCount()` returns how many are registered.
- `notifyObservers(event, data)` calls `update(event, data)` on each
  registered observer.
- These methods may be called from any thread at any time. A thread that
  subscribes while another thread is inside an observer's `update()` does
  not wait for it, and the running notification still completes normally.
  The newcomer gets the next event.

| thread A | thread B | outcome |
|---|---|---|
| `notifyObservers("tick", 1)`, held inside the first observer | `addObserver(newcomer)` | B returns at once; A then reaches the other observers; `newcomer` gets nothing yet |
| `notifyObservers("tick", 2)` | | every observer, `newcomer` too, gets it |
