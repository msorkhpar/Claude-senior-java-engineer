`ExecutorService` needs its whole lifecycle each time: create, submit, `shutdown()`,
`awaitTermination`. The page's violation pastes it into `computeSquares` and
`computeCubes`, and warns what diverging copies do: one forgets `shutdown()` and leaks
threads, another returns results before its tasks complete. Its fix writes the lifecycle
**once**, in `ParallelComputation.executeAll`:

```java
ExecutorService executor = Executors.newFixedThreadPool(threadCount);
try {
    List<Future<T>> futures = executor.invokeAll(tasks);
    ...
} finally {
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
}
```

Complete `ParallelComputation`:

- `executeAll(tasks, threadCount)` runs every task on a pool of `threadCount` threads and
  returns their results **in task order**, not in the order the tasks happen to finish. A
  failed task becomes a new `RuntimeException` that wraps the `ExecutionException` from `get()`, as the
  page's `new RuntimeException("Task execution failed", e)` does, so its cause's cause is the task's
  exception, even an unchecked one. A
  `threadCount` of zero or less throws `IllegalArgumentException`, even for an empty task list,
  and an empty task list with a valid count gives an empty list;
- **the pool is shut down before `executeAll` returns**, so no thread outlives the call.
  (Since Java 19, `ExecutorService` is `AutoCloseable`, so try-with-resources is fine
  too.);
- `squares(numbers, threadCount)` and `cubes(numbers, threadCount)` build their tasks and
  reuse `executeAll`: neither has a lifecycle of its own. (The tests see their results, not how
  they were computed, so the reuse is the page's lesson rather than a graded step.)

Examples:

- `squares(List.of(1, 2, 3, 200), 1)` is `[1, 4, 9, 40000]`, and
  `cubes(List.of(2, 3), 1)` is `[8, 27]`;
- with two threads, when the second task finishes before the first, the result is still
  `[first, second]`;
- once `executeAll` returns, the pool's threads have ended.
