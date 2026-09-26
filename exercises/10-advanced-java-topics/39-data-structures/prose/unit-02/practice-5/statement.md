Quicksort is O(n log n) on average but O(n^2) when every pivot is the smallest
or largest element, which a first-element pivot does on already sorted input.
The page's fix takes the median of the first, middle and last elements as the
pivot (the tests grade the running time it buys, not the choice itself). Its
edge-case list adds all-identical input, where a partition that
sends every equal element to one side degrades too; a partition that stops on
elements equal to the pivot from both ends makes sure **equal elements are split
between both sides**.

Write `QuickSort.sort(values, compare)`:

- Compare only through `compare.applyAsInt(a, b)`; the tests count the calls.
- Return a new sorted array; the input is left unchanged.
- **Already sorted input stays O(n log n)**, and so do **reversed** and
  all-identical input: on each of these 2,048-element inputs use at most
  `4 * n * log2(n)` = 90,112 comparisons.

| values | result |
|---|---|
| `[5, 2, 8, 1, 9, 3, 7, 4, 6]` | `[1, 2, 3, 4, 5, 6, 7, 8, 9]` |
| `[1, 2, 3, 4, 5, 6, 7, 8, 9]` | the same, sorted |
| 2,048 sorted / reversed / identical values | sorted, at most 90,112 comparisons |
