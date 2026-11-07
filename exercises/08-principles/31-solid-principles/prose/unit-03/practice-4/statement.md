Several teams plug their own `TaskQueue` implementations into the dispatcher, which is written
against the interface. Last month one of them took the dispatcher down under load, and every
team's own tests had been green. CI now runs `TaskQueueContract.breaches` on each implementation,
and two of its checks are unfinished.

Write `rejectsNull` and `offerNeverThrows` in `src/main/java/practice/TaskQueueContract.java`.
Each answers whether the queue it is given keeps that part of the contract written on
`TaskQueue`, so that a queue the dispatcher can rely on is reported with no breaches and any
other is reported. About twelve lines in total.
