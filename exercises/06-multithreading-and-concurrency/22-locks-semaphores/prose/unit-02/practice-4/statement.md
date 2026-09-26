A semaphore's permits belong to nobody: **any thread** may `release()`, and a
`release()` that comes **before** any `acquire()` is not lost, it simply raises
the count. The page's start gate uses `new Semaphore(0)`, and its edge cases
note that the count may even start **below zero**: `new Semaphore(-2)` needs
two releases before one acquire can succeed.

Write `Rendezvous`, which lets a coordinator wait until `workers` workers have
each reported ready, using one `Semaphore`:

- `void ready()` is called once by each worker, from any thread, at any time,
  even before the coordinator waits.
- `boolean awaitAll(long timeout, TimeUnit unit)` waits **at most** `timeout`
  for every worker to be ready, and returns whether they all were.

| workers | calls | answer |
|---|---|---|
| 3 | three threads each call `ready()`; then `awaitAll(1, SECONDS)` | `true` |
| 3 | `ready()` twice; `awaitAll(100, MILLISECONDS)` | `false` |
| 3 | then a third `ready()`; `awaitAll(3, SECONDS)` | `true` |
| 2 | nobody ready; `awaitAll(100_000, MICROSECONDS)` | `false` after about 100 ms |
| 2 | a coordinator waits in `awaitAll(8, SECONDS)`; two other threads call `ready()` | the coordinator gets `true` |
