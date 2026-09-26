The page's most duplicated concurrency code is the lock-try-finally pattern, pasted into
`deposit()`, `withdraw()` and `getBalance()`:

```java
lock.lock();
try { balance += amount; return balance; }
finally { lock.unlock(); }
```

Its fix is a `LockExecutor`: the pattern is written **once**, and each caller passes only
its business logic as a lambda. Complete it:

- `withLock(Supplier<T> action)` acquires the lock, runs the action and returns its
  result, and releases the lock in a `finally` block, so **the lock is released even when
  the action throws**;
- `withLockRun(Runnable action)` does the same for an action with no result;
- both refuse a `null` action with `NullPointerException` **before** acquiring the lock;
- `Account` keeps a balance, and every one of its operations (`deposit`, `withdraw` and
  `getBalance`) goes through its `LockExecutor`. A read is guarded like a write: an
  operation that skips the shared mechanism is exactly the copy that diverges. `deposit` and
  `withdraw` return the balance they made, computed while the lock is held.

Examples:

- `withLock(() -> 1234)` returns `1234`, and the action runs while the lock is held;
- after an action throws `IllegalStateException`, the exception reaches the caller and the
  lock is free;
- `withLock(null)` throws `NullPointerException` and never takes the lock;
- an account opened with `1000` gives `1500` after `deposit(500)`, `1200` after
  `withdraw(300)`, and `getBalance()` is then `1200`, with the lock taken once per
  operation.
