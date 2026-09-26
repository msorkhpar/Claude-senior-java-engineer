The page defines a **data race**: two threads access the same variable, at least one
access is a write, and there is **no happens-before** between the two accesses. The JMM
does not make a racy program undefined, but it lets the reader see stale values.

A trace is the list of actions one run performed, in the order they ran, as in the
previous practice: each `Action` has a thread, a `Kind` (`READ`, `WRITE`,
`VOLATILE_READ`, `VOLATILE_WRITE`, `LOCK`, `UNLOCK`, `START`, `JOIN`) and a target (a
variable, a monitor, or the thread started or joined). Happens-before follows the page's
six rules: program order, monitor lock, volatile variable, thread start, thread join,
and transitivity. You may reuse your happens-before from the previous practice.

Write `DataRaces.find(List<Action> trace)`. It returns every data race as a pair
`[i, j]` of trace indices with `i < j`, sorted by `i` then `j`. Only `READ` and `WRITE`
are plain accesses; a volatile access is a synchronization action and never races.

The page's broken flag, where neither `ready` nor `data` is volatile:

| # | thread | action |
|---|---|---|
| 0 | T1 | `WRITE data` |
| 1 | T1 | `WRITE ready` |
| 2 | T2 | `READ ready` |
| 3 | T2 | `READ data` |

| trace | answer |
|---|---|
| the broken flag | `[[0, 3], [1, 2]]` |
| the same, with `ready` volatile | `[]` |
