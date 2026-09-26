In concurrent code, DIP lets you swap the execution model. A `JobScheduler` depends on an
`ExecutionStrategy` abstraction, and the thread model is a detail behind it:

```java
interface ExecutionStrategy {
    <T> Future<T> execute(Callable<T> task);
    void shutdown();
}

// Switch thread model without touching JobScheduler
var scheduler = new JobScheduler(new VirtualThreadStrategy()); // or FixedPoolStrategy
```

In `Jobs`, write:

- `VirtualThreadStrategy`, which runs each task on a virtual thread through one
  `Executors.newVirtualThreadPerTaskExecutor()`; its `shutdown()` shuts that executor down,
  so a task handed to it afterwards is refused with `RejectedExecutionException`;
- `JobScheduler(strategy)`, which takes its strategy through the constructor and rejects
  `null` there. `scheduleJob(job)` counts the job when it is scheduled (not when it
  runs) and hands that same job to **the strategy it was given**, `getJobCount()` returns the count, and `shutdown()` shuts the strategy down. (Jobs may be scheduled
  from many threads, so count them thread-safely; the tests cannot force two counts to collide.)

The scheduler never creates an executor of its own. That is what makes it testable: as the
page says, a test can inject a strategy that runs each job on the caller's own thread, and
then nothing about the test depends on thread timing.
