`join()` waits until a thread ends, and waits **forever** if it never does. The
page's safer form is a bounded `join(millis)`, then `isAlive()` to see whether the
thread is still running, and `interrupt()` to ask it to stop. Two edge cases from
the page matter here: `join()` on a thread that was never started returns at once
(it waits only while the thread is alive), and `join(0)` means "wait forever".

Write `Deadline.await(Thread worker, long millis)`, returning an `Outcome`:

- `NOT_STARTED` if `worker` was never started (do not wait);
- `FINISHED` if it ends within `millis` milliseconds;
- `CANCELLED` if it is still running after `millis`: interrupt it first.

A `millis` of `0` or less throws `IllegalArgumentException`.

| worker | `millis` | result |
|---|---|---|
| started, ends quickly | `5000` | `FINISHED` |
| started, blocked until interrupted | `100` | `CANCELLED`, and the worker is interrupted |
| never started | `100` | `NOT_STARTED` |
| any | `0` | `IllegalArgumentException` |
