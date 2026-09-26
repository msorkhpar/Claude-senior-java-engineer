The page's critical insight: **local variables are always thread-local**. They live in
the stack frame of the thread running the method, so no other thread can see them. Heap
variables (instance fields, static fields, array elements) are shared, and every thread
using the same object uses the same fields. Its `processLocally` example keeps its running
`sum` in a local variable for exactly that reason.

One `Stats` object is shared by every thread of a service. Write `Stats.sum(Iterable<Integer>
values)`, which returns the total of the values as a `long`. Many threads call it on the
same `Stats` at the same time, each with its own values, and no call may disturb another
or wait for another.

| values | answer |
|---|---|
| `[1, 2, 3]` | `6` |
| `[]` | `0` |
| `[2147483647, 1]` | `2147483648` |
| thread A sums `[10, 20]` while thread B sums `[1, 2, 3]` | A gets `30`, B gets `6` |
