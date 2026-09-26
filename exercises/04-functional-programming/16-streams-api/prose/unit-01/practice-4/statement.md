Since Java 16, `Stream.toList()` collects a stream into an **unmodifiable** list, a
shorter and safer ending than `collect(Collectors.toList())` for a result nobody
should change.

Write `CleanNames.clean(List<String> names)`. It returns the names trimmed of
surrounding spaces (spaces inside a name stay), in their order, repeats included, leaving out `null` entries and names that are
empty once trimmed. The list it returns must be unmodifiable.

| names | answer |
|---|---|
| `["  hello  ", "world", " Ann"]` | `["hello", "world", "Ann"]` |
| `["  hello  ", null, "  world  ", "  ", null]` | `["hello", "world"]` |

Think about the order of the steps: what must be gone before you call a method on an
entry, and when a name counts as empty.
