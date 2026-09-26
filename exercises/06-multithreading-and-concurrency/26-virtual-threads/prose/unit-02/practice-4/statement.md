Tools built for platform threads can miss virtual threads: the page's pitfall is
`Thread.getAllStackTraces()`, which does not include them at all. The page's
advice is to name virtual threads through a builder factory
(`Thread.ofVirtual().name("order-processor-", 0).factory()`) and to watch them
with tools that do see them. Here you build a small one yourself.

Write `CountingFactory`, a `ThreadFactory` for
`Executors.newThreadPerTaskExecutor(factory)`:

- `new CountingFactory(prefix)`; `newThread(r)` returns an unstarted **virtual**
  thread named `prefix + n`, where `n` counts the threads made so far, from `0`.
- `running()` is the number of this factory's threads whose task has started
  and not yet ended. A task that **throws** has ended too.

| steps | answer |
|---|---|
| `new CountingFactory("order-")`, three tasks submitted | threads `order-0`, `order-1`, `order-2`, all virtual |
| while the three tasks are blocked | `running()` is `3` (and `Thread.getAllStackTraces()` shows none of them) |
| after the executor is closed | `running()` is `0` |
| a thread whose task throws, after it ends | `running()` is `0` |
| two threads made, never started | `running()` is `0` |
