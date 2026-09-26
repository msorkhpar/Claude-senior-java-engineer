A `Collections.synchronizedList` wrapper locks **every single call** on the
wrapper object, but the page's first pitfall is that **iteration is many
calls**: the iterator is not protected, and another thread's `add` in the
middle of a for-each loop can throw `ConcurrentModificationException`. The
page's fix names the lock the wrapper itself uses.

Write two methods for a list made by `Collections.synchronizedList(...)` that
other threads may change at any time:

- `Wrapped.mapAll(syncList, f)` returns a **new** list holding `f` applied to
  each element, in order. No other thread may change the list while you walk
  it, and a change another thread makes during the walk must not break it.
- `Wrapped.snapshot(syncList)` returns a **new** list with the list's current
  elements, which later changes to `syncList` do not affect.

| syncList | call | answer |
|---|---|---|
| `[a, b, c]` | `mapAll(syncList, String::toUpperCase)` | `[A, B, C]` |
| `[a, b, c]`, another thread calls `add("d")` while `f` runs on `a` | `mapAll(syncList, String::toUpperCase)` | `[A, B, C]`; afterwards the list is `[a, b, c, d]` |
| `[a, b, c]` | `s = snapshot(syncList)`, then `syncList.add("d")` | `s` is still `[a, b, c]` |
