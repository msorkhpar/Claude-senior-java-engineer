The batch scheduler's dashboard has a "next up" panel. Operators use it to decide whether to
hold a job before it starts, and the panel has to show exactly the jobs the worker will take
next, in the order it will take them. `JobQueue` already serves the worker through `next()`.

Finish the `TODO` in `src/main/java/practice/JobQueue.java`: `upcoming(k)`, the `k` jobs due
soonest, soonest first, or all of them when fewer are queued.

When you're done, the panel matches what the worker then does, and looking at the panel never
changes what the worker gets. About four lines in total.
