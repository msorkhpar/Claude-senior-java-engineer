The payments service has an admin page that sorts the JVM's threads into three groups: **active**,
**idle**, and **stuck behind another thread**. During the last incident it showed 0 stuck, while
every payment request was waiting for one thread to finish, and on-call restarted the service
without looking further. On a quiet night, it must not call the idle mailer workers stuck either.

The page reads a snapshot from `ThreadMXBean.getThreadInfo(...)`, and `ThreadTriage` already
counts and groups the threads. Finish the `TODO` in `src/main/java/practice/ThreadTriage.java`:

- `classify(ThreadInfo thread)`: which of the three activities this thread is in.

A thread is stuck behind another when it can't go on until another thread lets go of something
it holds; `stuckBehind` then reports it under that thread's name. About three lines in total.

The tests start real threads, put each one somewhere (computing, in a lock, in a queue, in a
sleep) and hand your method the snapshot `ThreadMXBean` takes of them.
