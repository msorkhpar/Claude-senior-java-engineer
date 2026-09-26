The page contrasts `map()` and `flatMap()`: on a `List<List<String>>`, `map`
keeps the nesting, while `flatMap(Collection::stream)` turns it into one
`Stream<String>`. It also warns that null elements must be filtered out
(`filter(Objects::nonNull)`) before a stream works on them.

Write `Tags.allTags(List<List<String>> posts)`. Each inner list is the tags of
one post. Return every tag of every post as one list that is:

- upper-cased with **`toUpperCase(Locale.ROOT)`**, so the result does not depend on
  the machine's default locale (in Turkish, `"i"` upper-cases to `"İ"`),
- free of duplicates, where two tags are duplicates when they are **equal after upper-casing**
  (the same text, even if each is its own `String` object),
- sorted in natural (alphabetical) order,
- without empty tags (`""`) and without `null` tags. A tag of spaces such as `" "` is
  not empty: keep it as it is.


| posts | answer |
|---|---|
| `[["streams", "java"], ["lambda"]]` | `["JAVA", "LAMBDA", "STREAMS"]` |
| `[["Java"], ["JAVA", "jvm"]]` | `["JAVA", "JVM"]` |
| `[["", "x"], [null]]` | `["X"]` |
| `[]` | `[]` |
