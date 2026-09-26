Cleanup that must happen whatever the outcome belongs in a `finally` block. A
`java.util.concurrent.locks.ReentrantLock` is not `AutoCloseable`, so try-with-resources cannot
release it: that is still `finally`'s job.

Write the class `Vault`, holding a balance behind a lock:

- `Vault(int balance)` starts with that balance;
- `int withdraw(int amount)` takes the lock, and then:
  - throws `IllegalArgumentException("amount must be positive")` when `amount <= 0`;
  - throws `IllegalStateException("insufficient funds")` when `amount` is more than the balance;
  - otherwise lowers the balance by `amount` and returns the new balance;
- `int balance()` returns the balance, and `boolean isLocked()` says whether the lock is held.

| calls on `new Vault(100)` | answer |
|---|---|
| `withdraw(30)` | `70` |
| then `isLocked()` | `false` |
| then `withdraw(500)` | throws `IllegalStateException("insufficient funds")` |

However `withdraw` ends, the lock must be free afterwards and the balance must make sense.
