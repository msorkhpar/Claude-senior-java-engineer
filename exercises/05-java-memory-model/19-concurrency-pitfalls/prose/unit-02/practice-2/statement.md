`if (sold < capacity) sold++` is a check-then-act: two threads can both pass the
check for the last ticket. The page's fix is a **compare-and-set retry loop**:
read the current value, decide, then `compareAndSet(current, next)`. The CAS fails
when another thread changed the value in between, so the update loops: it re-reads,
**re-checks its condition** (the page's `incrementIfPositive` gives up when the
condition no longer holds), and tries again.

Write `TicketCounter`. It counts sold tickets in a `Cell`, a tiny interface with
`get()` and `compareAndSet(expected, next)`; `Cell.atomic()` is one backed by an
`AtomicInteger` (already written for you). The constructor takes the cell and the
capacity.

- `sell()` sells one ticket and returns `true`, or returns `false` when all
  `capacity` tickets are sold. Update the cell **only** with `compareAndSet`, in a
  retry loop.
- `sold()` returns how many tickets are sold.

| calls (capacity 2) | answer | `sold()` |
|---|---|---|
| `sell()` | `true` | 1 |
| `sell()` | `true` | 2 |
| `sell()` | `false` | 2 |

The tests hand you a cell whose first `compareAndSet` loses to a sale made by
"another thread", to check what your loop does next.
