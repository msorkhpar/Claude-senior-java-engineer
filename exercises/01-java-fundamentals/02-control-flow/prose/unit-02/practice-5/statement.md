Nested `for` loops walk a 2D array: the outer loop over the rows, the inner loop over the
columns of the current row, `grid[row].length` of them. A plain `break` leaves only the
inner loop; a labeled `break` leaves both.

Write `find(int[][] grid, int target)` in `Grid`. It returns `{row, col}` of the first
cell equal to `target`, reading row by row from the top and left to right, or `{-1, -1}`
when no cell holds it. Rows may have different lengths.

| grid | target | answer |
|---|---|---|
| `{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}` | `5` | `{1, 1}` |
| `{{1, 7}, {7, 2}, {3, 7}}` | `7` | `{0, 1}` (the first one) |
| `{{1}, {2, 3, 4}, {5}}` | `4` | `{1, 2}` |
| `{{1, 2}, {3, 4}}` | `9` | `{-1, -1}` |
