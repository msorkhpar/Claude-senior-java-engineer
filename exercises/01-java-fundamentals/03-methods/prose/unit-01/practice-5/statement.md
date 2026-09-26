Boundary values are where numeric methods break. `(a + b) / 2` looks right, but `a + b`
overflows an `int` when both are large, and `/` rounds toward zero, not down.

Write `midpoint(int a, int b)` in `Midpoint`. It returns the average of `a` and `b`,
rounded down (toward negative infinity):

- `midpoint(2, 8)` is `5`, and `midpoint(3, 4)` is `3`;
- `midpoint(-3, 0)` is `-2`, since -1.5 rounds down to -2;
- the extremes work: `midpoint(Integer.MAX_VALUE, Integer.MAX_VALUE - 2)` is
  `2147483646`, and `midpoint(Integer.MIN_VALUE, Integer.MAX_VALUE)` is `-1`.
