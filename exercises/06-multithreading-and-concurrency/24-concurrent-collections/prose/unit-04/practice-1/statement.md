The page's HashMap always rounds a requested capacity **up** to the nearest
power of two, so that `hash & (capacity - 1)` can stand in for `hash % capacity`.
Its `tableSizeFor` gives 16 for 10, 32 for 17, and 64 for 64. A request of 0 is
valid and gives the minimum table.

Write `TableSize.tableSizeFor(int requested)`: the smallest power of two that
is at least `requested`, and `1` for a request of zero or less.

| requested | answer |
|---|---|
| `10` | `16` |
| `17` | `32` |
| `100` | `128` |
| `64` | `64` |
| `0` | `1` |
