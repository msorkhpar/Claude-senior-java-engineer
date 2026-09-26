The page says the Java Memory Model reasons about **actions** and a **happens-before**
order over them: if action X happens-before action Y, X's writes are visible to Y. Its
Q2 answer lists the edges:

1. **Program order**: in one thread, an earlier action happens-before a later one.
2. **Monitor lock**: an unlock of a monitor happens-before every *later* lock of that
   *same* monitor.
3. **Volatile**: a write to a volatile field happens-before every *later* read of that field.
4. **Thread start**: `t.start()` happens-before every action of thread `t`.
5. **Thread join**: every action of thread `t` happens-before `t.join()` returning.

Happens-before is **transitive**.

Write `HappensBefore.happensBefore(List<Action> trace, int x, int y)`. The `trace` is one
execution: every action of every thread, in the order they happened. Each `Action` names
its thread, its `Kind` and its target: a variable for reads and writes, a monitor for
`LOCK`/`UNLOCK`, and a thread name for `START`/`JOIN`. Return whether action `trace.get(x)`
happens-before action `trace.get(y)`. An action does not happen-before itself.

The page's `HappensBeforeDemo`, as a trace:

| # | thread | action |
|---|---|---|
| 0 | A | `WRITE value` |
| 1 | A | `VOLATILE_WRITE ready` |
| 2 | B | `VOLATILE_READ ready` |
| 3 | B | `READ value` |

| x, y | answer |
|---|---|
| `0, 3` | `true` (program order, the volatile edge, program order) |
| `3, 0` | `false` |
| `0, 1` | `true` |

With `ready` a plain field (`WRITE ready` and `READ ready`), `0, 3` is `false`.
