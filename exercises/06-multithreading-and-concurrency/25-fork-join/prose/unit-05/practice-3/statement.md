The page's Q5 splits a range into **N subtasks at once** instead of two, and
runs them all at the same time. Its way: fork all the chunks except the last
one, compute the last chunk in the current thread, then join the forked chunks
(in reverse order).

Its edge cases add: with **more ways than elements**, reduce the number of ways
to the number of elements; and with an odd size, one chunk gets the extra
elements.

Write `MultiWaySum(array, start, end, ways, threshold, probe)`, a
`RecursiveTask<Long>` summing `array[start..end)`:

- if the range has at most `threshold` elements, or `ways <= 1`, it is a
  **leaf**: call `probe.leaf(start, end)` once, then sum it sequentially;
- otherwise use `w = min(ways, length)` chunks of `length / w` elements each,
  the **last chunk running to `end`**, and make each chunk a leaf task
  (`ways = 1`).

| array | ways | threshold | answer | leaves |
|---|---|---|---|---|
| `1..12` | 3 | 2 | `78` | `[0,4) [4,8) [8,12)` |
| `1..10` | 3 | 2 | `55` | `[0,3) [3,6) [6,10)` |
| `5, 6, 7` | 8 | 1 | `18` | `[0,1) [1,2) [2,3)` |

The tests also check, in a pool of three workers, that three chunks are all
inside `probe.leaf` **at the same time**.
