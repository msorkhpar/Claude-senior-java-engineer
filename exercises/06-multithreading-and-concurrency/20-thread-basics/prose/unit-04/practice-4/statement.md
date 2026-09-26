The page reads a thread dump by its states: **many threads `BLOCKED` on one
monitor** means lock contention, and threads `BLOCKED` in a **circular chain**,
each waiting for a lock the next one holds, means a deadlock. A thread parked on
a `ReentrantLock` shows as `WAITING`, not `BLOCKED`.

`Snapshot(String name, Thread.State state, String lock, String lockOwner)` is
one thread of a dump: `lock` is the lock it waits for and `lockOwner` the thread
holding it, both `null` when it waits for none. Write, in `DumpReader`:

- `Map<Thread.State, Integer> countByState(List<Snapshot> dump)`: how many
  threads are in each state, with **all six** states as keys.
- `Optional<String> hottestMonitor(List<Snapshot> dump)`: the lock that the most
  **`BLOCKED`** threads wait for; on a tie, the alphabetically first. Empty when
  no thread is `BLOCKED`.
- `List<String> deadlocked(List<Snapshot> dump)`: the names, sorted, of the
  threads on a cycle of `BLOCKED` threads, each waiting for a lock held by the
  next.

| dump | `hottestMonitor` | `deadlocked` |
|---|---|---|
| A `BLOCKED` on L1 held by B; B `BLOCKED` on L2 held by A; C `RUNNABLE` | `L1` | `[A, B]` |
| A, B `BLOCKED` on L1 held by C; D, E, F `WAITING` on L2 held by C | `L1` | `[]` |
| A on L1 held by B, B on L2 held by C, C on L3 held by A, all `BLOCKED` | `L1` | `[A, B, C]` |
| the first dump plus D `BLOCKED` on L2 held by A | `L2` | `[A, B]` |
