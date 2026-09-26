The page's best practice is to keep `compute()` pure: each task returns its
own result and the parent combines the results at the join, instead of writing
into shared state. Its edge cases warn that max and min must handle **negative
numbers** (start from `Integer.MIN_VALUE` / `Integer.MAX_VALUE`), and that
input is validated **in the constructor**.

Write `MinMaxTask(array, start, end, threshold)`, a `RecursiveTask<Range>` where
`Range` is the record `(min, max)` of `array[start..end)`. Split at the midpoint
while the range is longer than `threshold`, and combine the two halves'
`Range`s into one. An empty range has no smallest value: the
constructor refuses it with `IllegalArgumentException`.

| array | threshold | answer |
|---|---|---|
| `3, -1, 7, 2, 9, 0` | 2 | `Range[min=-1, max=9]` |
| `-5, -3, -9, -4` | 1 | `Range[min=-9, max=-3]` |
| `Integer.MIN_VALUE, Integer.MIN_VALUE` | 1 | `Range[min=-2147483648, max=-2147483648]` |
| `5, 3, 9, 4` | 1 | `Range[min=3, max=9]` |
| any array, range `[2, 2)` | 1 | `IllegalArgumentException` |
