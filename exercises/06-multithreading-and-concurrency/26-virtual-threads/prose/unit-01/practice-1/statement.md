Java 21 builds virtual threads with the `Thread.ofVirtual()` builder. The builder
can name every thread it makes: `name("http-handler-", 0)` names the first thread
`http-handler-0`, the next `http-handler-1`, and so on. Virtual threads are always
**daemon** threads, so nothing keeps the JVM alive for them: whoever starts them
must wait for them, and a thread that may never finish is joined with a timeout.

Write two methods in `Launch`:

- `startAll(String prefix, long first, List<Runnable> tasks)` starts one
  **virtual** thread per task and returns the started threads in task order. The
  thread for `tasks.get(i)` is named `prefix + (first + i)`.
- `awaitAll(List<Thread> threads, Duration limit)` waits for the threads, but
  never much longer than `limit` in total, and returns the threads that are still
  alive when it stops waiting (in their list order; empty when all finished).

| call | answer |
|---|---|
| `startAll("job-", 7, [a, b])` | two started virtual threads, `job-7` running `a` and `job-8` running `b` |
| `awaitAll(threads, 6 s)`, each thread runs for about 2 s | `[]`, after about 2 s, all threads finished |
| `awaitAll([stuck], 200 ms)`, `stuck` never ends | `[stuck]`, after about 200 ms |
