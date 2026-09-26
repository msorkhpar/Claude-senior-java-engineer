The page says streams are lazy: intermediate operations run only when a
terminal operation pulls elements, and operations such as `findFirst()` and
`anyMatch()` are short-circuiting. Its pitfall is collecting a whole
filtered list just to look at its first element, which processes the entire
stream.

Write `FirstMatch.first(List<String> raw, Function<String, String> clean, Predicate<String> wanted)`.
It applies `clean` to the elements of `raw`, in order, and returns the first
cleaned value that `wanted` accepts, or an empty `Optional` if none does.

- `clean` may be expensive: call it only on the elements up to and including
  the first match, never on the rest.
- `raw` may contain `null` elements: skip them without calling `clean` on them.

| raw | clean | wanted | answer |
|---|---|---|---|
| `[" a ", " bb ", " ccc ", " dd "]` | `String::strip` | length is 2 | `Optional[bb]`, and `clean` ran twice |
| `[" a ", " b "]` | `String::strip` | length is 2 | `Optional.empty` |
| `[null, " x "]` | `String::strip` | not empty | `Optional[x]` |
