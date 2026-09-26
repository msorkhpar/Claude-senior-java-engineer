Binary search is the page's O(log n) example: compare the target with the
middle element and **keep only the half that can still hold it**, so a million
elements take about 20 probes. Two edge cases from the page: a range of **one
element must still be checked**, and `(left + right) / 2` **overflows** on huge
ranges, so the middle is `left + (right - left) / 2`.

Write `BinarySearch.indexOf(values, target)`. `values` is a sorted
`BinarySearch.SortedInts` (given): `size()` and `get(i)`. Return the index of
`target`, or `-1` if it is absent. The tests count every `get` call.

| values | target | result |
|---|---|---|
| `[1, 3, 5, 7, 9]` | 7 | 3 |
| `[1, 3, 5, 7, 9]` | 4 | -1 |
| `[1, 3, 5, 7, 9]` | 9 | 4 |
| `[7]` | 7 | 0 |
| `[]` | 7 | -1 |
| 1,000,000 values | any | at most 20 `get` calls |
