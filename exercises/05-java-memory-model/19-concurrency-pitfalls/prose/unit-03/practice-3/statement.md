The page's `WrongMonitor` writes under `writeLock` and reads under `readLock`.
Both methods are `synchronized`, yet there is no happens-before between them:
an unlock is only seen by a later lock **of the same monitor**. The fix,
`SameMonitor`, uses one lock for both.

Write `SharedCell`, an `int` cell shared between threads. Every method locks
**the cell itself** (`this`), so a caller can make several calls one atomic
step by holding the cell's monitor:

```java
synchronized (cell) {
    if (cell.get() == 0) {
        cell.set(1);
    }
}
```

- `set(int value)` stores the value.
- `get()` returns the value last stored.
- `addAndGet(int delta)` adds `delta` and returns the new value, as one step.

| calls | result |
|---|---|
| `get()` on a new cell | `0` |
| `set(5)`, then `get()` | `5` |
| `set(5)`, then `addAndGet(3)` | `8` |
| another thread holds `synchronized (cell)` while you call `get()` | `get()` waits until it lets go |
