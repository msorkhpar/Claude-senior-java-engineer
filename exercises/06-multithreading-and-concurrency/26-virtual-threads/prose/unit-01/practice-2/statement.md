With virtual threads a blocking task no longer needs a pooled thread: the page's
advice is one new virtual thread per task, from
`Executors.newVirtualThreadPerTaskExecutor()` in a `try`-with-resources block,
whose `close()` waits for every task. A fixed pool of virtual threads is the
pitfall to avoid: it caps how many tasks run at once, exactly like a pool of
platform threads.

Write `Fanout.fetchAll(List<String> keys, Function<String, String> fetch)`:

- It calls `fetch` once per key, each call on its **own virtual thread**, and
  all the calls run **at the same time** (no cap on how many).
- It returns the results in key order, after every call has finished.

The tests see that each call runs on a virtual thread and that every call is in flight at once; whether a
thread is ever reused for a later call is not something they can force, so use the page's executor anyway.
- If a call throws, `fetchAll` throws an `ExecutionException` whose cause is
  that exception.

| keys | fetch | answer |
|---|---|---|
| `["a", "b", "c"]` | `k -> k.toUpperCase()` | `["A", "B", "C"]` |
| 1000 keys | each call waits until all 1000 have started | 1000 results |
| `["a", "b"]` | throws `IllegalStateException` on `"b"` | `ExecutionException`, cause the `IllegalStateException` |
