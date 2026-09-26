The page warns that `Executors.newFixedThreadPool(n)` queues without limit, and
recommends building a `ThreadPoolExecutor` yourself: a **bounded** queue, a
rejection policy, and a `ThreadFactory` that gives the threads useful names. Its
interview answer walks a pool of core 2, maximum 4 and a queue of 2 through seven
tasks.

Write `BoundedPool.create(core, max, queueCapacity, name)`, returning a
`ThreadPoolExecutor` that:

- keeps `core` threads and grows to at most `max` threads;
- holds at most `queueCapacity` waiting tasks;
- refuses a task it cannot take by throwing `RejectedExecutionException`;
- names its threads `name-1`, `name-2`, ... in the order it creates them, as
  daemon threads.

| `create(2, 4, 2, "orders")`, then tasks that stay busy | pool size | queued |
|---|---|---|
| after 4 tasks | `2` | `2` |
| after 6 tasks | `4` | `2` |
| a 7th task | `RejectedExecutionException` | |
| names of the threads running the first 6 tasks | `orders-1` .. `orders-4` | |
