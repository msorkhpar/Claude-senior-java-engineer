The page warns that a `HashSet` does **not** keep insertion order: it rejects
duplicates, but it iterates in whatever order its buckets give. A set compares
elements with `equals` and `hashCode`, never with `==`.

Write `Dedupe.distinct(List<String> words)`. It returns a **new** list holding
each distinct word once, in the order the words were **first seen**. Two words
are the same word when their text is equal, even if they are different
`String` objects. The input list is not changed.

| words | answer |
|---|---|
| `["a", "b", "c", "c"]` | `["a", "b", "c"]` |
| `["d", "c", "b", "a"]` | `["d", "c", "b", "a"]` |
| `["b", "a", "b"]` | `["b", "a"]` |
| `[new String("a"), new String("a")]` | `["a"]` |
