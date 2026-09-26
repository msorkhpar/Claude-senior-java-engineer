The page's Q4 turns the brute-force O(n^2) "check every pair" into O(n): walk
the array once, and for each element look up its *complement*
(`target - nums[i]`) in a HashMap of the values seen so far, mapped to their
indices.

Write `TwoSum.find(nums, target)`: return `{i, j}` with `i < j` and
`nums[i] + nums[j] == target`. Each test input has at most one such pair.

- **An element never pairs with itself**: index `i` is used once.
- **Two equal values at different indices can pair.**
- **No pair gives an empty array** (`new int[0]`), never `null`.

| nums | target | result |
|---|---|---|
| `[2, 7, 11, 15]` | 9 | `[0, 1]` |
| `[-3, 4, 3, 90]` | 0 | `[0, 2]` |
| `[3, 2, 4]` | 6 | `[1, 2]` |
| `[3, 3]` | 6 | `[0, 1]` |
| `[1, 2, 3]` | 100 | `[]` |
