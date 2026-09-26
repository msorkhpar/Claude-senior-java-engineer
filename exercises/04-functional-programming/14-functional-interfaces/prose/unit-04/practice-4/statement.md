`Predicate.isEqual(target)` tests `Objects.equals(target, t)`: it is null-safe on **both** sides, so
`Predicate.isEqual(null)` matches exactly the `null` inputs. Wrapped in `Predicate.not(…)` it matches everything
else.

Write two methods in `Matches`; `Predicate.isEqual` is the page's tool for both:

1. `countEqual(List<T> items, T target)` returns how many items equal `target`.
2. `without(List<T> items, T target)` returns the items that do not equal `target`, in order.

| items | target | `countEqual` | `without` |
|---|---|---|---|
| `["Alice", "Bob", "Alice", "Charlie"]` | `"Alice"` | `2` | `["Bob", "Charlie"]` |
| `["Alice", "Bob"]` | `"Zoe"` | `0` | `["Alice", "Bob"]` |

Lists may hold `null`, and `target` itself may be `null`.
