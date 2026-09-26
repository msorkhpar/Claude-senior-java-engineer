An exception thrown out of a thread's `run()` ends **that** thread. It never
reaches the thread that called `start()`: it goes to the thread's
`UncaughtExceptionHandler`, and with none set it is only printed to `System.err`,
"silently lost" from the application's point of view. The page's advice is to
always set a handler.

Write `Crash.runAndReport(String name, Runnable task)`. Run `task` on a new thread
named `name`, wait for that thread to finish, and return what it threw as
`"<thread name>: <message>"`, or `null` if it finished normally. Anything the task
throws counts, `Error`s included.

| task | result |
|---|---|
| `() -> { throw new RuntimeException("disk full"); }`, name `importer` | `"importer: disk full"` |
| `() -> { }`, name `quiet` | `null` |
| `() -> { throw new AssertionError("invariant broken"); }`, name `checker` | `"checker: invariant broken"` |
