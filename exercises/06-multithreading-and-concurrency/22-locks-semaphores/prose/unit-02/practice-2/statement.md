For rate limiting the page advises `tryAcquire()` **without blocking**: reject
the excess work instead of queueing it. Two of its pitfalls apply at once:
release the permit in `finally`, or one exception leaks it for ever; and only
release what you acquired, or the count inflates beyond its limit.

Write `Bulkhead`, which lets at most `limit` tasks run at the same time:

- `<T> Optional<T> tryCall(Supplier<T> task)` runs `task` if a slot is free
  **right now** and returns its result; if every slot is busy it returns
  `Optional.empty()` at once, without waiting at all (not even a short timed
  wait) and without running the task. Whatever the task throws, an exception
  or an `Error`, reaches the caller, and the slot is given back either way.
- `int available()` is the number of free slots.

| limit | situation | call | answer |
|---|---|---|---|
| 2 | idle | `tryCall(() -> "done")`, `available()` | `Optional[done]`, `2` |
| 1 | another task is running | `tryCall(() -> "x")` | `Optional.empty` at once; the task did not run |
| 1 | idle | `tryCall(() -> { throw new IllegalStateException(); })`, then `available()` | throws; then `1` |
| 1 | a call was rejected while another ran; that one finished | `available()` | `1` |
