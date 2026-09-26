`start()` is legal only on a thread in the `NEW` state. A thread that is running,
or has already finished (`TERMINATED`), throws `IllegalThreadStateException` when
started again: a `Thread` object is one execution, a one-way trip from `NEW` to
`TERMINATED`. The page's fix is a **new** `Thread` instance.

Write `Keeper.ensureStarted(Thread thread, Runnable job)`. `thread` was created for
`job`. Return the thread that is running, or has just been started to run, the job:

- `thread` never started: start it and return it;
- `thread` still running: return it unchanged, and start nothing;
- `thread` finished: start a **new** thread with the same name for `job`, and return that.

| `thread` | result |
|---|---|
| `new Thread(job, "report-1")`, never started | the same thread, now started |
| running | the same thread; nothing new runs |
| finished | a different, started thread named like the old one |
