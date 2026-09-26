A daemon thread is a background thread that does not keep the JVM alive: when every
non-daemon thread has finished, the JVM exits. The page stresses that
`setDaemon(true)` **must be called before `start()`**; it is a property of how a
thread runs, so it is too late once the thread has been started.

Write `Daemons.markBackground(Thread thread)`. If `thread` has never been started,
make it a daemon and return `true`. If it has been started (whether it is still
running or has already finished), leave it unchanged and return `false`. It never
throws.

| thread | result | `isDaemon()` afterwards |
|---|---|---|
| `new Thread(task, "metrics-monitor")`, not started | `true` | `true` |
| started, still running | `false` | `false` |
| started and finished | `false` | `false` |
