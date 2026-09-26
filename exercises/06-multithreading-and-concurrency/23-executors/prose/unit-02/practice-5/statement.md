The page's last interview answer re-submits a failed `Callable` up to a maximum
number of retries, with **a new `Future` for each attempt**, and a timeout on
each attempt. When every attempt has failed it throws the last failure,
unwrapped from its `ExecutionException`, unless the cause is an `Error` (which
is not an `Exception`): then the `ExecutionException` itself is thrown.

Write `Retry.call(executor, task, maxRetries)`:

- make at most `maxRetries + 1` attempts, each a fresh `submit(task)`, waiting
  at most 5 seconds for each;
- an attempt that is still running after 5 seconds counts as failed (its failure is the
  `TimeoutException`), and is
  cancelled with an interrupt so it does not hold the executor's thread;
- return the first attempt's result that succeeds;
- if all fail, throw the **last** attempt's exception as described above.

Leave out the page's backoff sleep.

| `task`, `maxRetries` | `call(executor, task, maxRetries)` | attempts |
|---|---|---|
| fails twice, then returns `"ok"`; `3` | `"ok"` | `3` |
| always fails; `2` | throws | `3` |
| attempt `n` throws `new IOException("attempt " + n)`; `2` | throws `IOException("attempt 3")` | `3` |
| hangs on the first attempt, then returns `"ok"` (single-thread executor); `1` | `"ok"` after about 5 s | `2` |
| throws `new AssertionError("broken")`; `0` | throws `ExecutionException` with that cause | `1` |
