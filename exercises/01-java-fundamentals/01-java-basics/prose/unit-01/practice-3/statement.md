Write `IntegerMean.mean(int[] values)`. It returns the mean of the values as an
`int`, truncated the way integer division truncates (toward zero).

| values | answer |
|---|---|
| `{2, 4, 6}` | `4` |
| `{1, 2}` | `1` |
| `{-3, -4}` | `-3` |

Two things the page warns about apply here:

- **Integer division by zero throws** `ArithmeticException`. An empty array has
  no mean: return `0` for it rather than throwing.
- **Integer overflow is silent.** The mean of `{Integer.MAX_VALUE, Integer.MAX_VALUE}`
  is `Integer.MAX_VALUE`, but their sum does not fit in an `int`.
