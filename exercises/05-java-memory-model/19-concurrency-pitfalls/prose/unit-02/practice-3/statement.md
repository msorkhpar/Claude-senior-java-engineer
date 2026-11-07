A connection pool keeps at least `min` connections open and never more than `max`. Operators
change the limits by hand and the autoscaler changes them on its own, from different threads,
and every change must also pass the platform's `Policy` (a ceiling per service tier, for
example). Last week one service ran with `min` 16 and `max` 12 for an hour: an operator raised the
minimum while the autoscaler lowered the maximum, and both changes were accepted. Each limit is
an `AtomicInteger` today.

Finish `limits()`, `setMin(...)` and `setMax(...)` in `src/main/java/practice/PoolLimits.java`.
You may change how the limits are stored. The policy check calls the config service and can take
seconds, and a change must not wait for another change's policy check. When you're done, the
limits can never be seen with `min` above `max`, every change that was accepted is in force, and a
change that a concurrent change made invalid is refused. About twelve lines in total.

The tests make one change's policy check slow and make another change while it waits.
