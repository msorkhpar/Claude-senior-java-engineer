Loop bounds are where off-by-one errors live. An array of length `n` has `n - k + 1`
windows of `k` neighbouring values, and the last one starts at index `n - k`.

Write `best(int[] readings, int k)` in `Windows`. It returns the largest sum of `k`
neighbouring readings. `k` is always between `1` and `readings.length`.

| readings | k | answer |
|---|---|---|
| `{1, 5, 2, 3, 1}` | `2` | `7` (5 + 2) |
| `{4, 1, 1, 1, 9}` | `2` | `10` (1 + 9, the last window) |
| `{3, 2}` | `2` | `5` (the one window) |
| `{-4, -1, -7}` | `1` | `-1` |
