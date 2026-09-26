`Collectors.joining(delimiter, prefix, suffix)` concatenates a stream of strings through one `StringBuilder`,
with the delimiter only *between* elements and the prefix and suffix around the whole result.

Write two static methods in `SqlText`:

1. `String inClause(List<Integer> ids)`: the ids in list order, separated by `", "`, inside parentheses.
2. `String nameList(List<String> names)`: the names that are not blank, in list order, separated by `", "`,
   inside square brackets.

| call | answer |
|---|---|
| `inClause([1, 2, 3, 4])` | `"(1, 2, 3, 4)"` |
| `inClause([7])` | `"(7)"` |
| `nameList(["Alice", "Bob", "Charlie"])` | `"[Alice, Bob, Charlie]"` |

Consider an empty id list, and names that are empty or made only of spaces. Blank means `String.isBlank()`:
empty, or only whitespace, Unicode spaces such as U+2003 included. Every id and every kept name appears as it
was given, repeats included, and a kept name keeps its own surrounding spaces.
