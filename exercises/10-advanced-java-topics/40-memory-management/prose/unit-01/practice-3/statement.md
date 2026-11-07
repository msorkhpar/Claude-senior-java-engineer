The building gateway keeps the last day of readings from every sensor in memory: one reading
every 500 ms, a time in epoch milliseconds and a `double` value, 172,800 readings per sensor per
day. The plan is 40,000 sensors on one gateway. The prototype stored each sensor's day in about
36 bytes per reading, which doesn't fit, and its dashboard query, "when was this sensor above the
limit?", slowed the gateway down with garbage collections every few seconds.

`capacity()` and `size()` are done. Finish the `TODO`s in `src/main/java/practice/ReadingLog.java`:
the storage and the constructor, `append(...)`, `timeAt(...)`, `valueAt(...)` and
`timesAbove(...)`.

When you're done, a full day of readings allocates at most 17 bytes per reading, and one query
allocates at most 8 bytes per reading in the log plus 8 bytes per time it returns. About fifteen
lines in total.

The tests read the thread's allocation counter
(`com.sun.management.ThreadMXBean.getCurrentThreadAllocatedBytes`) around the calls they measure.
