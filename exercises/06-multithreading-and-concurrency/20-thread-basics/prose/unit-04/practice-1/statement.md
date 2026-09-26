The page lists exactly six `Thread.State` values and what triggers each: `start()`
makes a `NEW` thread `RUNNABLE`; trying to enter a `synchronized` block that
another thread holds makes it `BLOCKED`; waiting with no timeout (`wait()`,
`join()`, `park()`) makes it `WAITING`, and waiting with one (`sleep(ms)`,
`wait(ms)`, `await(time, unit)`) makes it `TIMED_WAITING`; when `run()` ends it
is `TERMINATED`. And a thread waiting for a `ReentrantLock` is `WAITING`, not
`BLOCKED`, because the lock parks it.

Write `States.enter(Thread.State state, Object monitor, ReentrantLock lock,
CountDownLatch done)`. It returns a daemon thread that is in `state` (or soon
gets there) and stays in it until the test lets it go:

| `state` | the thread you return |
|---|---|
| `NEW` | is not started |
| `RUNNABLE` | is started and keeps working until `done` reaches zero |
| `BLOCKED` | is started and tries to enter `synchronized (monitor)`; the test holds `monitor` |
| `WAITING` | is started and tries to take `lock`; the test holds `lock` |
| `TIMED_WAITING` | is started and waits on `done` for at most one minute |
| `TERMINATED` | has been started and has finished before `enter` returns |

The test counts `done` down and releases `monitor` and `lock` when it has seen
the state; each started thread must then finish.
