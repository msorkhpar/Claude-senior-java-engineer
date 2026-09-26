The page's `CopyOnWriteArrayList` iterator works on a **snapshot**: the array
the list had when the iterator was created. Every later `add`, `remove` or
`set` builds a new array, so the iterator never sees those changes and never
throws `ConcurrentModificationException`. The list itself, read again, does
show them.

Write `Walk.visitAll(CopyOnWriteArrayList<String> list, Consumer<String> onVisit)`.
It calls `onVisit` on each element the list held **when the walk began**, in
order, and returns those elements as a new list. `onVisit` may add to or
remove from `list` while the walk runs.

| list | onVisit | answer | list after |
|---|---|---|---|
| `[a, b, c]` | does nothing | `[a, b, c]` | `[a, b, c]` |
| `[a, b, c]` | adds `"x" + s` for each `s` | `[a, b, c]` | `[a, b, c, xa, xb, xc]` |
| `[a, b, c]` | on `"a"`, removes `"c"` | `[a, b, c]` | `[a, b]` |
