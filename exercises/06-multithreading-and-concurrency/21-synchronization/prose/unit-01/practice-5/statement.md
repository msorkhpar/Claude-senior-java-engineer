The page's `ProducerConsumerMonitor` coordinates a producer and a consumer on one
monitor: each waits with `wait()` **in a loop** while it cannot go on, because a
thread that wakes up may find the condition false again. The page wakes the other
side with `notifyAll()`, and it warns never to swallow an `InterruptedException`.

Write `BoundedBuffer<T>`, a first-in, first-out buffer of at most `capacity`
items, on one monitor:

- `void put(T item)` adds an item, **waiting while the buffer is full**.
- `T take()` removes and returns the oldest item, **waiting while it is empty**.
- `int size()` is the number of items held.
- A thread interrupted while it waits in `put` or `take` leaves with the
  `InterruptedException`.

| calls, capacity 2 unless noted | answer |
|---|---|
| `put("a")`, `size()`, `take()`, `size()` | `1`, `"a"`, `0` |
| capacity 3: `put("a")`, `put("b")`, `put("c")`, then three `take()` | `"a"`, `"b"`, `"c"` |
| capacity 1: `put("x")`, then another thread's `put("y")` | that `put` waits until a `take()` returns `"x"` |
| empty: another thread's `take()`, then `put("z")` | that `take` waits, then returns `"z"` |
| empty: another thread's `take()`, then that thread is interrupted | its `take` throws `InterruptedException` |
| empty: two other threads each call `take()`, then `put("z")` | one of them returns `"z"`; the other keeps waiting |
