The page asks you to **draw the recursion tree** for an array size and a
threshold, and contrasts a **balanced** (50/50) split with an **unbalanced**
(10/90) one, whose tree is deeper and has more tasks. Page 6.6.1.2 estimates a
midpoint split's tree as depth `d = ceil(log2(N / T))`, `2^d` leaves and
`2^(d+1) - 1` tasks; that estimate is exact only when every halving is even.

Write `SplitTree.shape(n, threshold, percent)`. It walks the split of the range
`[0, n)` exactly as a task would and returns its `Shape(depth, leaves, tasks)`:

- a range of at most `threshold` elements is a **leaf**;
- a longer range `[start, end)` splits at
  `start + (end - start) * percent / 100`; if that is not past `start`, it
  splits at `start + 1`, so the recursion always makes progress;
- `depth` is the deepest leaf's level (the root is level 0), `leaves` counts
  leaves, `tasks` counts every range, leaves included.

| n | threshold | percent | answer |
|---|---|---|---|
| 10000 | 1000 | 50 | `Shape[depth=4, leaves=16, tasks=31]` |
| 1000 | 1000 | 50 | `Shape[depth=0, leaves=1, tasks=1]` |
| 1000 | 100 | 10 | `Shape[depth=23, leaves=24, tasks=47]` |
| 20 | 1 | 10 | `Shape[depth=18, leaves=20, tasks=39]` |
| 7 | 3 | 50 | `Shape[depth=2, leaves=3, tasks=5]` |
