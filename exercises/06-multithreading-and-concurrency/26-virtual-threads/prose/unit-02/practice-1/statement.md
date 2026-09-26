Virtual threads implement the whole `Thread` API, but two of its settings are
fixed: a virtual thread is **always a daemon** (`setDaemon(false)` throws
`IllegalArgumentException`) and always runs at **`Thread.NORM_PRIORITY`**
(`setPriority` is silently ignored). `Thread.ofVirtual()` has no `daemon()` or
`priority()` methods at all. The page's rule of thumb follows: use a virtual thread
by default, and a platform thread where you need a non-daemon thread or a priority.

`Spec(String name, boolean daemon, int priority)` is given. Write
`Threads.create(Spec spec, Runnable task)`, returning an **unstarted** thread that
runs `task`, named `spec.name()`:

- a **virtual** thread when the spec is a daemon at `NORM_PRIORITY`;
- otherwise a **platform** thread with the spec's daemon flag and priority.

| spec | answer |
|---|---|
| `Spec("worker-1", true, Thread.NORM_PRIORITY)` | unstarted virtual thread `worker-1` (daemon, `NORM_PRIORITY`) |
| `Spec("keeper", false, Thread.NORM_PRIORITY)` | unstarted platform thread `keeper`, not a daemon |
| `Spec("urgent", true, Thread.MAX_PRIORITY)` | unstarted platform thread `urgent`, daemon, `MAX_PRIORITY` |
| `Spec("background", true, Thread.MIN_PRIORITY)` | unstarted platform thread `background`, `MIN_PRIORITY` |
| `Spec("both", false, Thread.MAX_PRIORITY)` | unstarted platform thread `both`, not a daemon, `MAX_PRIORITY` |
