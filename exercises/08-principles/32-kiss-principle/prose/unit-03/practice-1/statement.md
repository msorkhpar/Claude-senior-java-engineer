The page's second pitfall is a state machine with many intermediate states, nested locks and
complex transition rules. Its fix: keep the states few, and make each transition one
`compareAndSet` on an `AtomicReference`:

```java
private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);

public boolean start() {
    return state.compareAndSet(State.IDLE, State.RUNNING);
}
```

Write `Job`, whose nested `enum State` is `IDLE`, `RUNNING`, `COMPLETED`, `FAILED`, and which
starts `IDLE`. Each transition returns `true` when it happened, and **`false`, with the state
left as it was, when the job is not in the state the transition starts from**:

- `start()`: `IDLE` to `RUNNING` only, so a second `start()` is refused, and so is a
  `start()` on a `COMPLETED` or `FAILED` job, which must be reset first;
- `complete()`: `RUNNING` to `COMPLETED` only, so it is refused on an `IDLE`, `FAILED` or
  already `COMPLETED` job;
- `fail()`: `RUNNING` to `FAILED` only, refused likewise on `IDLE`, `COMPLETED` or `FAILED`;
- `reset()`: `COMPLETED` or `FAILED` back to `IDLE`, and from nowhere else;
- `state()` returns the current state.

Example: a new job's `complete()` is `false` and it stays `IDLE`; `start()` then
`complete()` then `reset()` are all `true`, and the job is `IDLE` again.

Making each transition one `compareAndSet`, rather than a `get()` followed by a `set()`, is
what keeps it correct when two threads race for the same transition. A race cannot be
forced from a test, so that part is this page's quiz; the tests here check the transitions.
