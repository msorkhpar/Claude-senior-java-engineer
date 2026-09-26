The page's Q2 builds `MergeSortTask extends RecursiveTask<int[]>`, which
**returns** the sorted values:

1. Base case: if `array.length <= threshold`, sort a **copy** with
   `Arrays.sort` and return it.
2. Otherwise copy the left and right halves into their own arrays, make a task
   for each, run them (the page forks the left, computes the right and joins
   the left), and **merge** the
   two sorted halves into one new array.

Write `MergeSortTask(array, threshold)`. The caller's array is never changed,
and the task never hands it back as its result.

| array | threshold | answer |
|---|---|---|
| `12, -3, 45, 0, 7, -18, 33, 5, 21, -9` | 3 | `-18, -9, -3, 0, 5, 7, 12, 21, 33, 45` |
| `3, 1, 2` | 4 | a new array `1, 2, 3`; the input still reads `3, 1, 2` |
| `4, 1, 4, 1, 2, 4, 2, 1` | 2 | `1, 1, 1, 2, 2, 4, 4, 4` |
| `7` | 4 | a new array `7`, not the input itself |
| `[]` | 2 | `[]` |
