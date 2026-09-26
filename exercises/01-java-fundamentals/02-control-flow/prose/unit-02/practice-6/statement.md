A `double` used as a loop counter collects rounding error: adding `0.1` ten times gives
`0.9999999999999999`, not `1.0`. Drive the loop with an `int` counter and compute each
value from it instead.

Write `ticks(double start, double step, int count)` in `Ticks`. It returns `count` values,
where value `i` is `start + i * step`:

- `ticks(0.0, 0.5, 5)` is `{0.0, 0.5, 1.0, 1.5, 2.0}`;
- `ticks(0.0, 0.1, 11)` ends with exactly `1.0`;
- `ticks(3.0, 1.0, 0)` is `{}`.
