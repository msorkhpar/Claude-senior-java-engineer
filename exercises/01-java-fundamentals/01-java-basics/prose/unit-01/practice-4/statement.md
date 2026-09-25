`0.1 + 0.2 == 0.3` is `false` in Java: most decimal fractions have no exact
binary form, so a sum carries a tiny rounding error. Comparing floating-point
numbers with `==` is therefore a bug.

Write `NearlyEqual.nearlyEqual(double a, double b)`. It returns `true` when the
two values differ by **less than** `NearlyEqual.EPSILON` (`1e-9`), and `false`
otherwise.

| a | b | answer |
|---|---|---|
| `0.1 + 0.2` | `0.3` | `true` |
| `1.0` | `1.1` | `false` |
| `1.0` | `1.000001` | `false` |

Two special values need thought:

- `Double.POSITIVE_INFINITY` is nearly equal to itself (and to nothing else),
  and the same holds for negative infinity. Note that `Infinity - Infinity` is `NaN`.
- `Double.NaN` is nearly equal to **nothing**, not even to itself, just as
  `NaN == NaN` is `false`.
