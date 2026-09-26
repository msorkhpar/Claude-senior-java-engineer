A concrete strategy is one algorithm behind the strategy interface. The page
asks strategies to be **pure**: no side effects on what the caller passed in.
Its edge cases are `null` data, an empty list and a one-element list.

Write `BubbleSort<T extends Comparable<T>>`:

- `sort(data)` returns a **new list**, which the caller may change, holding `data`'s elements in ascending
  order (duplicates kept), sorted with bubble sort: repeatedly swap
  neighbours that are out of order.
- It **never changes** `data`, and it returns a new list even when `data` is
  empty or has one element.
- `sort(null)` throws `IllegalArgumentException`.
- `name()` returns `"BubbleSort"`.

| data | answer | `data` afterwards |
|---|---|---|
| `[5, 3, 8, 1, 3]` | `[1, 3, 3, 5, 8]` | `[5, 3, 8, 1, 3]` |
| `[]` | `[]` (a new list) | `[]` |
| `[42]` | `[42]` | `[42]` |
| `null` | `IllegalArgumentException` | |
