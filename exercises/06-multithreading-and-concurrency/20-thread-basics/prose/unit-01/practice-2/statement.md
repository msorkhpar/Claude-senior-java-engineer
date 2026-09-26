Java 21 made virtual threads stable. The page's advice is to put **I/O-bound**
work (network calls, database queries, file I/O) on cheap virtual threads, which
the JVM multiplexes onto a few carrier threads, and to keep **CPU-bound** work
on platform threads, which map one-to-one to OS threads. The `Thread.Builder`
API (`Thread.ofVirtual()`, `Thread.ofPlatform()`) creates either kind. Virtual
threads are unnamed by default, so name them yourself.

Write `Launcher.start(Work work, String name, Runnable task)`. It starts a
thread named `name` that runs `task`, and returns it: a virtual thread for
`Work.IO_BOUND`, a platform thread for `Work.CPU_BOUND`.

| call | returned thread |
|---|---|
| `start(Work.IO_BOUND, "handler-1", task)` | virtual, named `handler-1`, runs `task` |
| `start(Work.CPU_BOUND, "cruncher-1", task)` | platform, named `cruncher-1`, runs `task` |
