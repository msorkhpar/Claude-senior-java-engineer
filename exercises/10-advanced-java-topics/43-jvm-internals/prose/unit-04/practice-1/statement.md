The payments service hangs a few times a month, and each time the on-call engineer scrolls through
a `jcmd <pid> Thread.print` of four hundred threads, looking for the one that everybody else is
waiting for. The dump has no deadlock section, because there is no cycle. We want a tool that
reads the dump and answers that question directly.

`DumpReader` already splits a dump into threads and joins the answers into a report. Finish the
two `TODO`s in `src/main/java/practice/DumpReader.java`:

- `waitingFor(thread)`: the monitor the thread is blocked trying to enter, or `null`;
- `held(thread)`: the monitors the thread holds while the dump is taken.

When you're done, `whoBlocksWhom(dump)` maps each thread that is blocked on a `synchronized` lock
to the threads that really hold that lock, and to no one else. Threads that sleep, park or wait
for a notify are not blocked by anybody. About fifteen lines in total, including any helper you
add to read a lock line.

The tests feed it thread blocks cut from a real Java 21 dump of a payments program, so print one
with `jcmd` from a small program of your own if you want to see every kind of line first.
