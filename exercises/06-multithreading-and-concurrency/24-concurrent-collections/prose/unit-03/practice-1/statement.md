The page's Q6 explains how an `ArrayList` iterator fails fast. The list keeps a
counter, `modCount`, that every **structural** change (an add or a remove)
increments; `set` is not structural. An iterator copies `modCount` into its own
`expectedModCount` when it is created, and `next()` throws
`ConcurrentModificationException` when the two differ. The iterator's own
`remove()` changes the list and then updates `expectedModCount`, so the
iterator stays valid.

`TrackedList` is a small array-backed list of strings; storage, `add`,
`removeAt`, `set`, `get` and `size` are written for you. Complete it:

- make the list keep `modCount` for its structural changes, and
- write `iterator()`: `hasNext()`, `next()` (throws `NoSuchElementException`
  past the end, `ConcurrentModificationException` on a stale `modCount`) and
  `remove()` (removes the element the last `next()` returned; a second
  `remove()` without a `next()` in between throws `IllegalStateException`).

| calls | answer |
|---|---|
| list `[a, b, c]`, iterate | `a`, `b`, `c` |
| `it = iterator()`, `it.next()`, `add("d")`, `it.next()` | throws `ConcurrentModificationException` |
| `it.next()`, `set(1, "B")`, `it.next()` | `"B"`, no exception |
| `it.next()`, `it.remove()`, `it.next()` | `"b"`; the list is `[b, c]` |
