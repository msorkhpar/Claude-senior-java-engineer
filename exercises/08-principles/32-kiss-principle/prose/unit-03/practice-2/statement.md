The nightly settlement jobs all append to one ledger file, so each job runs through a `Gate`
that lets one job in at a time. Last night settlement hung. The ops dashboard, which reads
`ThreadMXBean`, showed six job threads `RUNNABLE` at full CPU inside `Gate.inside` and no thread
owning anything; it took an hour of reading stacks to find `settle-eu`, the job that was inside,
waiting on a file share. The same week, a job that threw on a malformed ledger line was
followed by a gate nobody could get through.

Rewrite `inside` in `src/main/java/practice/Gate.java`. When you're done, jobs still run one at a
time; a thread waiting at the gate is reported by `ThreadMXBean` as waiting for a lock, held by
the thread of the job that is inside; and a job that throws passes its exception to its caller
without keeping the gate closed for the next job. About three lines.
