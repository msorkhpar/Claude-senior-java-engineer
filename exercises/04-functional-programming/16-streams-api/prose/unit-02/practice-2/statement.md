`Arrays.stream(int[] array, int from, int to)` streams a slice of a primitive array
as an `IntStream`, with no boxing. Primitive streams bring numeric terminal
operations with them: `sum()`, `average()` and `summaryStatistics()`.

Write `RangeSummary.summarize(int[] values, int from, int to)`. It returns a
`Summary(long count, long sum, double average)` for the values at indexes `from`
(inclusive) to `to` (exclusive). The indexes are always valid for the array.

| values | from | to | answer |
|---|---|---|---|
| `{3, 1, 4, 1, 5, 9, 2, 6}` | `0` | `8` | `Summary[count=8, sum=31, average=3.875]` |
| `{3, 1, 4, 1, 5, 9, 2, 6}` | `1` | `4` | `Summary[count=3, sum=6, average=2.0]` |

An empty slice has a summary too (its average is `0.0`), and a slice of large values
may add up to more than an `int` holds.
