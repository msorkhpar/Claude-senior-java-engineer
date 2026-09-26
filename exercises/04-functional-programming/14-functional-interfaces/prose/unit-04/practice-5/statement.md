`IntPredicate` is `boolean test(int)`: the primitive form of `Predicate<Integer>`, with no boxing, and the type
`IntStream.filter` takes. It composes with `and()`, `or()` and `negate()`, but has no static `not()` or `isEqual()`.

Write three methods in `Ranges`:

1. `between(int lo, int hi)` returns an `IntPredicate` for `lo <= n <= hi`.
2. `outside(int lo, int hi)` returns its opposite; build it with `negate()`.
3. `select(int from, int to, IntPredicate keep)` returns the values `from, from + 1, …, to` that pass `keep`.

| call | result |
|---|---|
| `select(1, 6, n -> n % 2 == 0)` | `[2, 4, 6]` |
| `select(-5, 5, between(1, 5).and(n -> n % 2 == 0))` | `[2, 4]` |
| `select(-2, 2, outside(-1, 1))` | `[-2, 2]` |

Mind the bounds, and a range written backwards.
