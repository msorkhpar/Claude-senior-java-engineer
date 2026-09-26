The page's first pitfall: calling `list.remove(...)` inside a for-each loop
over an `ArrayList` throws `ConcurrentModificationException`. Its Q2 ranks the
safe ways to remove while iterating. Mind the page's other warnings too: a
forward index loop that removes shifts the next element under the cursor, and
a `CopyOnWriteArrayList` iterator throws `UnsupportedOperationException` from
`remove()`.

Write `Purge.removeAll(List<String> list, String target)`. It removes, **in
place**, every element whose text equals `target`, keeps the other elements in
order, and returns how many it removed. `list` may be an `ArrayList` or a
`CopyOnWriteArrayList`.

| list | target | answer | list after |
|---|---|---|---|
| `[a, b, c, b, d]` | `"b"` | `2` | `[a, c, d]` |
| `[b, b, a]` | `"b"` | `2` | `[a]` |
| `[a, c]` | `"b"` | `0` | `[a, c]` |
| `CopyOnWriteArrayList [a, b, c, b]` | `"b"` | `2` | `[a, c]` |
