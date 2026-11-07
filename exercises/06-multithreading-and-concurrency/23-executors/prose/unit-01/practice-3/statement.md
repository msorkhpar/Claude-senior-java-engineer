The invoice service takes invoice runs from a message broker and hands them to a worker pool.
On a deploy the orchestrator gives the old process a grace period, then kills it. Two things go
wrong today: runs that would have finished in a few seconds are cut off, and the runs that were
waiting in the pool are simply lost, so customers miss their invoices until someone notices.

Write `stop(pool, grace, unit)` in `src/main/java/practice/Drain.java`, called by the deploy's
shutdown hook:

- work the pool already has, running or waiting, gets the grace period to finish, and `stop`
  returns only when it has finished or the grace period is over;
- what is still running after the grace period is asked to stop, and gets one more grace period
  to do so; the runs that never started are returned, so the hook can put them back on the broker;
- if the orchestrator interrupts the hook while `stop` waits, stop the pool at once, return what
  never started, and leave the hook's thread able to see that it was interrupted.

About twelve lines in total. The tests call `stop` on a thread of their own and use a pool with one
worker.
