There are two ways to drop unwanted elements. `stream().filter(predicate)` builds a **new** list and leaves the
source alone. `collection.removeIf(predicate)` changes the collection **in place**, with no iterator and no
`ConcurrentModificationException`. `Predicate.not(String::isBlank)` reads well in a filter.

Write two methods in `Cleaner`. An entry is empty when it is `null` or `String.isBlank()` is true for it, so Unicode
whitespace such as `"\u2003"` counts as no text.

1. `meaningful(List<String> items)` returns a new list of the non-empty entries, in order; it is a new list even when
   nothing had to be dropped.
2. `prune(List<String> names)` removes the empty entries from `names` itself and returns how many it removed.

| call | result | list afterwards |
|---|---|---|
| `meaningful(["hello", "", "world"])` | `["hello", "world"]` | unchanged |
| `prune(["Alice", "", "Bob"])` | `1` | `["Alice", "Bob"]` |

Empty entries may sit next to each other. The page's own example list is `["Alice", "", "Bob", null, "  ", "Charlie"]`.
