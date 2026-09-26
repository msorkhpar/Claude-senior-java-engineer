When two fields must always agree, updating them one after the other is an
atomicity violation: a reader can see the new balance with the old transaction
count. The page's first fix (its `MultiVariableAtomicity`, approach 1) guards
**every** read and write of both fields with one lock: `synchronized` methods on the
object.

Write `Account`, with a balance and a count of successful transactions, both
starting at 0. Guard both fields with the account object's own monitor
(`synchronized` methods, or `synchronized (this)`):

- `deposit(amount)` adds to the balance and counts one transaction;
- `withdraw(amount)` takes from the balance and counts one transaction, or returns
  `false` and changes nothing when the balance is too small;
- `snapshot()` returns a `Snapshot(balance, transactions)` read in one step.

| calls | answer | `snapshot()` |
|---|---|---|
| `deposit(100)` | | `(100, 1)` |
| `withdraw(30)` | `true` | `(70, 2)` |
| `withdraw(500)` | `false` | `(70, 2)` |
