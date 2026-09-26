Cleanup that must happen whatever the outcome belongs in a `finally` block. A
`java.util.concurrent.locks.ReentrantLock` is not `AutoCloseable`, so try-with-resources cannot
release it: that is still `finally`'s job.

Write the class `Vault`, holding a balance behind a lock:

- `Vault(int balance)` starts with that balance (which may be negative, for an overdrawn vault);
- `int withdraw(int amount)` takes the lock, releases it in `finally`, and:
  - throws `IllegalArgumentException("amount must be positive")` when `amount <= 0`;
  - throws `IllegalStateException("insufficient funds")` when `amount` is more than the balance;
  - otherwise lowers the balance by `amount` and returns the new balance;
- `int balance()` returns the balance, and `boolean isLocked()` says whether the lock is held.

| calls on `new Vault(100)` | answer |
|---|---|
| `withdraw(30)` | `70` |
| then `isLocked()` | `false` |
| then `withdraw(500)` | throws `IllegalStateException("insufficient funds")` |

The tests run on one thread, so they make no thread-safety claim (which statements sit inside the
locked region is not graded); they check what one caller sees: the results above, and that however
`withdraw` ends, the lock is free afterwards and a refused withdrawal leaves the balance as it was,
whatever the numbers (compare `amount` with the balance; a subtraction can overflow).
