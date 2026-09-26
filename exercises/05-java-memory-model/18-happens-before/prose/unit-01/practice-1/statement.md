The page defines happens-before by six rules: **program order** inside one thread,
the **monitor lock** rule, the **volatile variable** rule, the **thread start** and
**thread join** rules, and **transitivity**. If A happens-before B, B is guaranteed to
see A's writes. Two actions with no happens-before between them are unordered, even if
one ran earlier on the clock.

A trace is the list of actions one run performed, in the order they ran. Each
`Action` has the thread that did it, a `Kind`, and a `target`:

| kind | target |
|---|---|
| `READ`, `WRITE` | a plain variable's name |
| `VOLATILE_READ`, `VOLATILE_WRITE` | a volatile variable's name |
| `LOCK`, `UNLOCK` | a monitor's name |
| `START`, `JOIN` | the name of the thread being started or joined |

Write `HappensBefore.happensBefore(List<Action> trace, int a, int b)`. It returns
`true` when the action at index `a` happens-before the action at index `b` by the six
rules, and `false` otherwise (an action does not happen-before itself). An edge from a
synchronization rule only runs forward in the trace: an unlock reaches the locks of the
same monitor **after** it, and a volatile write reaches the reads of the same variable
**after** it. `START t` happens-before every action of thread `t`; every action of
thread `t` happens-before a later `JOIN t`.

The page's own flag example, with `ready` volatile:

| # | thread | action |
|---|---|---|
| 0 | T1 | `WRITE data` |
| 1 | T1 | `VOLATILE_WRITE ready` |
| 2 | T2 | `VOLATILE_READ ready` |
| 3 | T2 | `READ data` |

| a, b | answer |
|---|---|
| `0, 1` | `true` (program order) |
| `1, 2` | `true` (volatile rule) |
| `0, 3` | `true` (transitivity) |
| `3, 0` | `false` |
