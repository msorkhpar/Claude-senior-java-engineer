The page's Q3 gives the rule for choosing between the two task types: if you
need the computation's result, use `RecursiveTask<V>`; if you are performing an
in-place transformation, use `RecursiveAction`. Rescaling an array needs both:
first a result (the largest value), then an in-place change.

Write `Percent.ofMax(pool, values, threshold)`. It runs two fork/join
computations in `pool`, each splitting at the midpoint while a range is longer
than `threshold`:

1. a `RecursiveTask<Integer>` that finds the **largest** value (the page's pairing: a task for the result, an action for the change);
2. a `RecursiveAction` that replaces every `values[i]` with
   `values[i] * 100 / max` (integer division), **in place**.

Values are never negative. If the largest value is `0`, leave the array alone.

| values | threshold | values afterwards |
|---|---|---|
| `5, 10, 20, 15, 0, 2` | 2 | `25, 50, 100, 75, 0, 10` |
| `0, 0, 0, 0` | 2 | `0, 0, 0, 0` |
| `50000000, 100000000, 25000000` | 1 | `50, 100, 25` |
| `2, 3` | 1 | `66, 100` |
| `[]` | 2 | `[]` |
