The page's producer-consumer pattern hands work between threads through a
`BlockingQueue`, which does all the synchronization. A **bounded** queue gives
backpressure: `put()` blocks while it is full, `take()` while it is empty. When
blocking forever is a risk (producers and consumers sharing one small pool),
the page's safer route is `offer(item, timeout)`: give up, and handle it.

Write `WorkBuffer<T>` with a fixed `capacity`:

- `put(item)` adds the item, **waiting** while the buffer is full.
- `take()` removes the oldest item, waiting while the buffer is empty.
- `offer(item, timeoutMs)` adds the item if room appears within the timeout
  and returns whether it did; `poll(timeoutMs)` returns the oldest item, or
  `null` if none appears within the timeout.
- `produced()` and `consumed()` count the items that went in and came out;
  `size()` is how many are waiting.

| capacity | calls | result |
|---|---|---|
| 2 | `put("a")`, `put("b")`, `take()`, `take()` | `"a"`, then `"b"` |
| 1 | `put("a")`, `offer("b", 50)` | `false`; `size()` is `1` |
| 1 | `poll(50)` on an empty buffer | `null` |
| 1 | `put("a")`, then `offer("b", 30000)` in another thread, then `take()` | the `offer` waits, then returns `true` |
| 1 | `poll(30000)` in another thread, then `put("z")` | the `poll` waits, then returns `"z"` |
| 1 | `put("a")`, then `put("b")` in another thread | it waits until a `take()` returns `"a"` |
