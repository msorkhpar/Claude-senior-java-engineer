The page's pitfalls warn about an action that **reads the array while other
subtasks modify it**, and its best practice is to work on **disjoint ranges**
so no synchronization is needed. A smoothing filter needs its neighbours'
values, so it cannot overwrite its input in place: it **reads** a source array
and **writes** a separate destination, each leaf writing only its own range.

Write `SmoothAction(source, dest, start, end, threshold)`, a `RecursiveAction`
that fills `dest[i]` for every `i` in `[start, end)`:

- `dest[i] = (source[i - 1] + source[i] + source[i + 1]) / 3` (integer division);
- the first and last index of the **whole array** have a missing neighbour, so
  they are copied: `dest[0] = source[0]`, `dest[n - 1] = source[n - 1]`.

Split at the midpoint with `invokeAll` while the range is longer than
`threshold`. `source` is never changed, and `dest` is written only in
`[start, end)`.

| source | range | threshold | dest afterwards (starting all 0) |
|---|---|---|---|
| `0, 9, 0, 9, 0` | `[0, 5)` | 5 | `0, 3, 6, 3, 0` |
| `0, 9, 0, 9, 0, 9, 0, 9` | `[0, 8)` | 2 | `0, 3, 6, 3, 6, 3, 6, 9` |
| `5, 1, 1, 1, 7` | `[0, 5)` | 5 | first `5`, last `7` |
| `3, 6, 9, 12, 15, 18, 21, 24` | `[2, 5)` | 2 | `0, 0, 9, 12, 15, 0, 0, 0` |
