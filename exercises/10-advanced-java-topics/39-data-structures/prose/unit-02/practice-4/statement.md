The page's example of **best, average and worst case**: insertion sort takes
each element in turn and moves it left past larger neighbours. On reversed input
every element passes all the earlier ones, n(n-1)/2 comparisons, O(n^2). On
already sorted input **each element stops at the first neighbour that is not
larger**, one comparison each, O(n).

Write `InsertionSort.sort(values, compare)`:

- Compare only through `compare.applyAsInt(a, b)` (negative, zero or positive,
  like `Integer.compare`); the tests count the calls.
- Move an element left only while its left neighbour is **strictly larger**,
  so **equal elements are never swapped** (the sort is stable).
- Return a new sorted array; **the input is left unchanged**.

| values | comparisons | result |
|---|---|---|
| `[5, 2, 8, 1, 9, 3, 7, 4, 6]` | | `[1, 2, 3, 4, 5, 6, 7, 8, 9]` |
| `[5, 4, 3, 2, 1]` | 10 | `[1, 2, 3, 4, 5]` |
| `[1, 2, ..., 1000]` | 999 | unchanged order |
