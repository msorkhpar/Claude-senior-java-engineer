`sorted()` uses natural order and throws `NullPointerException` on a `null` element;
`sorted(Comparator)` sorts by any rule, and comparators compose:
`Comparator.comparingInt(...).thenComparing(...)` breaks ties, and
`Comparator.nullsFirst(Comparator.naturalOrder())` places `null` before everything
else.

Write two methods in `Sorting`:

1. `byLengthThenAlpha(List<String> words)` sorts the words by length, shortest first,
   and words of equal length alphabetically.
2. `nullsFirst(List<String> names)` sorts the names in natural order, with every
   `null` before them.

| call | answer |
|---|---|
| `byLengthThenAlpha(["banana", "fig", "apple", "kiwi", "cat"])` | `["cat", "fig", "kiwi", "apple", "banana"]` |
| `nullsFirst(["banana", null, "apple"])` | `[null, "apple", "banana"]` |

A sort by length alone keeps equal-length words in the order they came in.
