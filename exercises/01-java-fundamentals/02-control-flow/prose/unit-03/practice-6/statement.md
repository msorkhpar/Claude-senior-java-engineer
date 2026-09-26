A loop invariant is a claim that holds before the loop and after every pass. Binary search
keeps one: *if the target is in the array, it lies between `low` and `high`*. Each pass
halves that range, and the loop ends when the range is empty.

Write `indexOf(int[] sorted, int target)` in `Search`, using a `while` loop. `sorted` is in
ascending order with no duplicates. Return the index of `target`, or `-1` if it is absent.

| sorted | target | answer |
|---|---|---|
| `{1, 3, 5, 7, 9}` | `7` | `3` |
| `{1, 3, 5, 7, 9}` | `1` | `0` |
| `{1, 3, 5, 7, 9}` | `9` | `4` |
| `{1, 3, 5, 7, 9}` | `4` | `-1` |
| `{}` | `4` | `-1` |
