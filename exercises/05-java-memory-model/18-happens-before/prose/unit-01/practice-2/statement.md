The platform team's replay harness records every run of a concurrency test as a list of actions,
in the order they ran: each thread's reads and writes of fields, its locks and unlocks, its
`start()` and `join()` calls. A race report built on the recording has to flag every pair of plain
accesses to one field, from two threads, at least one of them a write, that the memory model
leaves unordered. Today it is a stub: it can't tell an ordered pair from an unordered one.

`RaceReport.races()` already finds the pairs worth checking. Finish the two `TODO`s in
`src/main/java/practice/RaceReport.java`:

- `edge(i, j)`: whether a single rule of the memory model orders action `i` before the later
  action `j`, with no action in between;
- `happensBefore(i, j)`: whether the memory model orders action `i` before action `j` at all.

A recorded `JOIN` means the join returned because the other thread had ended. When you're done,
the report flags every pair the model leaves unordered and no pair it orders. About fifteen lines
in total.

The tests build recordings by hand; nothing in them runs concurrently.
