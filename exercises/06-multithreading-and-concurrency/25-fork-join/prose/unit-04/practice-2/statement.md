Every night the platform team scans the day's request latencies for requests slower than the
service's target and adds them to the operations dashboard's `Tally`. The `Tally` is shared by
the whole scan and takes a lock on every `report(...)` call. On a busy day the scan is slow, and a
profile shows the pool's workers spending most of their time waiting for that lock.

Finish `scanHere()` of the `Scan` task in `src/main/java/practice/SlowRequests.java`. It handles
one part of the day, on the current thread. When you're done:

- the tally gains exactly the slow requests of the day and the milliseconds they took, on top of
  whatever earlier scans reported;
- the scan's workers spend as little time on the tally's lock as the totals allow: the dashboard
  shows `reports()`, how many times the lock was taken.

About six lines in total.
