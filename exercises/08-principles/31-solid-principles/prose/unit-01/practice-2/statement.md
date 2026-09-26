When one class produces tasks, runs them **and** aggregates their results, each concern's
synchronization is mixed with the others. The page's pitfall is a class that owns its own
thread pool next to its business logic:

```java
class DataProcessor {
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    double computeAverage(List<Integer> data) { /* ... */ }
    void shutdown() { pool.shutdown(); }
}
```

Its fix is to **inject the `ExecutorService`**. Write the three classes nested in `Squares`:

- `TaskProducer.createTasks(count)` returns `count` tasks; task `i` (from 0) computes
  `i * i` as a `long`. A count of 0 gives an empty list; a negative count throws
  `IllegalArgumentException`.
- `TaskExecutor(ExecutorService)` runs tasks on the executor it is given, never on one of
  its own. `submitAll(tasks)` submits every task and returns their futures in order, and
  `shutdown()` shuts that executor down in the orderly way, letting tasks already
  submitted finish. However many tasks there are, every one goes to
  that executor.
- `ResultAggregator.sumResults(futures)` adds up the results.

For example, the 5 tasks `0, 1, 4, 9, 16` sum to `30`, and 100 000 tasks sum to
`333328333350000`, which needs a `long`.
