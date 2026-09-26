`p1.and(p2)` short-circuits like `&&`: if `p1` says `false`, `p2` is never asked. `p1.or(p2)` short-circuits like
`||`. Both refuse a `null` argument with a `NullPointerException` right where they are called.

Write two methods in `Rules`:

1. `allOf(List<Predicate<T>> rules)` returns a predicate that is true when every rule is true; `and()` is the page's tool.
2. `anyOf(List<Predicate<T>> rules)` returns a predicate that is true when at least one rule is true; `or()` is
   the page's tool.

| rules | predicate | `test(4)` | `test(3)` | `test(-2)` |
|---|---|---|---|---|
| `n > 0`, `n % 2 == 0` | `allOf` | `true` | `false` | `false` |
| `n > 0`, `n % 2 == 0` | `anyOf` | `true` | `true` | `true` |

A list may be empty, and rules may be expensive. A list holding `null` is a caller's bug: report it as early as
`and()` and `or()` themselves would.
