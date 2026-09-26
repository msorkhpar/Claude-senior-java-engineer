One `ReentrantLock` can hand out **several** `Condition` objects, each with its
own waiting threads: the page's `BoundedQueue` has `notFull` for putters and
`notEmpty` for takers. `await()` releases the lock while it waits;
`awaitNanos(n)` waits at most `n` nanoseconds and returns how many are left
(zero or less once the time is up). The page's pitfall: signalling the wrong
condition wakes nobody who can go on.

Write `BoundedQueue<T>`, a first-in, first-out queue of at most `capacity`
items, with one `ReentrantLock` and two conditions:

- `void put(T item)` waits while the queue is full.
- `T take()` waits while the queue is empty.
- `T poll(long timeout, TimeUnit unit)` waits **at most** `timeout` for an
  item and returns `null` if none came.
- `int size()`.

After a change, wake the side that can now go on: a put wakes a waiting taker,
and every removal, by `take` **or** `poll`, wakes a waiting putter.

| capacity | calls | answer |
|---|---|---|
| 2 | `put("a")`, `put("b")`, `size()`, `take()`, `take()` | `2`, `"a"`, `"b"` |
| 2 | another thread waits in `take()`; `put("x")` | that `take()` returns `"x"` |
| 1 | `put("a")`; another thread waits in `put("b")`; `take()` | `"a"`, and that `put` finishes |
| 2 | `poll(100_000, MICROSECONDS)` on an empty queue | `null` after about 100 ms |
| 2 | another thread waits in `poll(8, SECONDS)`; `put("z")` | that `poll` returns `"z"` |
| 1 | `put("a")`; another thread waits in `put("b")`; `poll(1, SECONDS)` | `"a"`, and that `put` finishes |
