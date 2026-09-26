Before Java 8, sorting with a custom order meant writing an anonymous inner class
that implements `Comparator<String>`. A lambda, or a method reference such as
`String::compareTo`, passes the same behaviour in one line.

Write two methods in `NameSorter`, each taking a `List<String>` and returning a
**new** sorted list:

1. `alphabetical(List<String> names)` orders the names alphabetically.
2. `byLength(List<String> names)` orders them by length, shortest first; names of
   the same length are ordered alphabetically.

| call | answer |
|---|---|
| `alphabetical(["Charlie", "Alice", "Bob"])` | `["Alice", "Bob", "Charlie"]` |
| `byLength(["aaa", "b", "cc"])` | `["b", "cc", "aaa"]` |

Think about what happens to the list the caller handed you, and about names
that tie on length.
