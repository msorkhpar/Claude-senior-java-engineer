`synchronized` gives two guarantees: **mutual exclusion** and **visibility** (an unlock
happens-before the next lock of the **same** monitor). The page's pitfalls show how both
are lost: locking a different object on one path, synchronizing writes but not reads, and
locking on something that is not a `private final` monitor. It also notes that a
`synchronized` block always releases its monitor when an exception leaves it.

Write the `Ledger` of an account:

- `deposit(long amount, Runnable whileLocked)` adds `amount` to the balance, then runs
  `whileLocked` **while still holding the lock** (an audit hook; tests use it to hold the
  lock open);
- `withdraw(long amount)` takes `amount` off the balance, or throws
  `IllegalStateException` when the balance is smaller, leaving the balance unchanged;
- `balance()` returns the balance.

Every one of them must use one and the same lock, so that no thread ever sees or changes
the balance while another thread is inside a deposit.

| calls | answer |
|---|---|
| `deposit(100)`, `withdraw(30)`, `balance()` | `70` |
| then `withdraw(500)` | `IllegalStateException`, and `balance()` is still `70` |
| a `balance()` from another thread while a deposit's hook runs | waits until the deposit leaves its lock |
