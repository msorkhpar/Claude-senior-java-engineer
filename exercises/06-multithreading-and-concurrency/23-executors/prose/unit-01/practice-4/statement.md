The page's table of implementations: `Executors.newSingleThreadExecutor()` has a
single thread and guarantees sequential, first-in first-out execution. Since
Java 19 an `ExecutorService` is also `AutoCloseable`, so try-with-resources shuts
it down and waits for its tasks. And the page asks you to name your threads with
a `ThreadFactory`.

Write `SerialWorker`, usable in try-with-resources:

- `new SerialWorker(name)` starts nothing yet; its one thread is a daemon named
  exactly `name`;
- `Future<T> submit(Callable<T> task)` queues a task; tasks run one at a time,
  in submission order, on that one thread;
- `close()` accepts no more tasks (a later `submit` throws `RejectedExecutionException`), and returns
  only once every task already submitted has run, however long that takes.

| calls on `new SerialWorker("audit")` | result |
|---|---|
| `submit(() -> 6 * 7).get()` | `42` |
| five tasks, the first slow, each noting its number and thread | `1, 2, 3, 4, 5`, all on one thread |
| a slow task and a queued one, then `close()` | `close()` waits; both tasks run |
| `submit(() -> Thread.currentThread().getName()).get()` | `"audit"` |
