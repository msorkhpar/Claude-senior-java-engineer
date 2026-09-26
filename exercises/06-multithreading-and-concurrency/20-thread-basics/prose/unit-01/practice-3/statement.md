The page asks you to name your threads ("db-worker-0", "db-worker-1", …) so thread
dumps and logs are readable, to set daemon status on purpose, and it shows Java
21's `Thread.Builder` doing all of that before a thread exists:
`Thread.ofPlatform().name("worker", 0).daemon(false).priority(Thread.NORM_PRIORITY)`.
A priority is a number from `Thread.MIN_PRIORITY` (1) to `Thread.MAX_PRIORITY` (10).

Write `Workers.factory(String prefix, boolean daemon, int priority)`. It returns a
`ThreadFactory` whose `newThread(task)` makes an **unstarted** platform thread for
`task`, named `prefix` followed by a counter that starts at `0` for each factory,
with the given daemon status and priority. A priority outside 1..10 throws
`IllegalArgumentException` from `factory` itself.

| call | threads made by three `newThread` calls |
|---|---|
| `factory("db-worker-", false, 5)` | `db-worker-0`, `db-worker-1`, `db-worker-2`; not daemon; priority 5 |
| `factory("metrics-", true, 10)` | `metrics-0`, …; daemon; priority 10 |
| `factory("x-", false, 11)` | `IllegalArgumentException`, no factory |
