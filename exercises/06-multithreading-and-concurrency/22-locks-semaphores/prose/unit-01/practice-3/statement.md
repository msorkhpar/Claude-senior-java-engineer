A `ReentrantLock` is **reentrant**: the thread that holds it can call `lock()`
again without blocking, and the lock keeps a **hold count**. Each `lock()`
adds one, each `unlock()` takes one away, and the lock is free only when the
count is back to zero, so the owner must unlock exactly as many times as it
locked. The page's `ReentrantExample` has `outer()` call `inner()`, both
locking the same lock.

Write `AuditLog`, guarded by the one **fair** `ReentrantLock` the starter gives
you (keep it fair: a thread already waiting for it goes before one that asks
later):

- `void record(String entry)` appends one entry under the lock.
- `void recordAll(List<String> entries, Runnable afterEach)` holds the lock for
  the **whole batch**, from before the first entry until after the last, so no
  other thread's entry lands in the middle of it. Locking around each entry
  separately is not enough: a waiting thread gets in between two entries. It
  records each entry by calling `record(entry)` (which takes the same lock
  again) and then calls `afterEach.run()`, still holding the lock.
- `String entries()` is every entry so far, joined by `","` (`""` when empty).

| calls | `entries()` |
|---|---|
| `recordAll(["a", "b"], hook)`, then `record("c")` | `"a,b,c"` |
| `recordAll(["a1", "a2"], hook)` while another thread calls `record("x")` after `"a1"` | `"a1,a2,x"` |
| `recordAll(["a", "b"], hook)`, then another thread's `record("c")` | `"a,b,c"`, and that `record` does not wait |
