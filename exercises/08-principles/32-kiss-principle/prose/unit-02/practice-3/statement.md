The page's KISS answer to "run a few things in parallel" is not a reactive framework, and
not a hand-tuned `ThreadPoolExecutor`: it is one virtual thread per task, in an executor
closed by try-with-resources.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var task : tasks) {
        executor.submit(() -> process(task));
    }
}
```

Write `runAll(List<Callable<T>> tasks)` in `Tasks`:

- **Every task runs at the same time as the others**: a task that waits until all the tasks
  have started must not hold the others back, however many tasks there are.
- **Each task runs on a virtual thread**: inside a task, `Thread.currentThread().isVirtual()`
  is `true`.
- It returns every task's result in the order of `tasks`, whichever finishes first. No
  tasks give an empty list.
- **A task that throws makes the whole call fail** with the `ExecutionException` its
  future reports, whose cause is the very exception the task threw. It does not skip that task
  and return the rest.

Example: three tasks returning `"result1"`, `"result2"` and `"result3"` give
`["result1", "result2", "result3"]`.
