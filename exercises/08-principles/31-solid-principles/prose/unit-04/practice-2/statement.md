A client that only submits tasks should not see `shutdown()` or the monitoring methods; a
component that manages the lifecycle should not see `submit()`. The page segregates a
concurrent executor into **roles**, and one class implements them all:

```java
class ManagedExecutor implements TaskSubmitter<Object>, LifecycleManageable, Monitorable {
    // ... implements all methods
}
```

In `Roles` the three role interfaces are given. Write:

- `ManagedExecutor(poolSize)`, on a fixed thread pool: `submit` runs a task on one of
  the pool's threads, never on the caller's, and counts it in `getCompletedCount()` only
  once it has finished (a task that throws has finished too), not when it is submitted; `shutdown()` shuts the pool down, so its
  threads end;
  `isShutdown()` says whether it was shut down. As the page's edge case says, a task
  submitted **after shutdown is refused** with `IllegalStateException`, guarded by an
  `AtomicBoolean`;
- `TaskClient`, which only submits: `runTask(task)` submits it and returns its future;
- `LifecycleManager`, which only manages the lifecycle: `gracefulShutdown()` and
  `isRunning()`.

The starter's two clients take a whole `ManagedExecutor`. **Each client's one constructor
must take only the role interface it uses**, so it can be handed anything that plays that
role.
