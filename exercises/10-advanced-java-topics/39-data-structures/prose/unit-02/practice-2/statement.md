`ArrayList.add` is **amortized O(1)**: when its array is full it allocates a
bigger one and copies every element (O(n) for that one call), but because
**the capacity grows by half of itself** each time, n adds copy only about 2n
elements in total. The page traces it from capacity 4: 4 -> 6 -> 9.

Write `IntList`:

- `new IntList(initialCapacity)`; a capacity below 1 is refused with
  `IllegalArgumentException`.
- `add(value)` appends; when the array is full it grows to
  `capacity + capacity / 2`, and **a growth step adds at least one slot**.
- `get(index)`; **only stored values can be read**: an index outside
  `0 .. size() - 1` throws `IndexOutOfBoundsException`.
- `size()`, `capacity()`, and `copies()`: how many elements all growth steps
  have copied so far.

| from capacity 4 | `size()` | `capacity()` | `copies()` |
|---|---|---|---|
| 4 adds | 4 | 4 | 0 |
| 5th add | 5 | 6 | 4 |
| 6th add | 6 | 6 | 4 |
| 7th add | 7 | 9 | 10 |
