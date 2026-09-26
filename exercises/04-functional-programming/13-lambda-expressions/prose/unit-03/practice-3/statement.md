Functional interfaces compose. `f.andThen(g)` runs `f` and then `g`, which is `g(f(x))`;
`f.compose(g)` runs `g` first, which is `f(g(x))`. Predicates combine with `and`, `or` and
`negate`, and `and` stops at the first `false`.

Write three static methods of `Compositions`:

1. `<T> Function<T, T> inOrder(List<Function<T, T>> steps)` returns one function that applies
   every step, first to last.
2. `Function<String, String> lengthLabel()` trims with `String.trim()`, lower-cases with
   `toLowerCase(Locale.ROOT)`, measures the `length()`, and formats it as `"Length: n"`. Both steps
   count: `trim()` removes only characters up to U+0020 (not a Unicode space such as U+2003), and
   lower-casing can change the length (`"\u0130"` becomes two chars).
3. `Predicate<String> acceptedName()` accepts a name that is not `null`, not empty (`isEmpty()`: a
   string of spaces is not empty), and either starts with a capital `"A"` or is longer than 5
   characters (six or more).

| call | answer |
|---|---|
| `inOrder(List.of(x -> x + 1)).apply(1)` | `2` |
| `inOrder(List.of(x -> x + 1, x -> x * 2)).apply(3)` | `8` |
| `lengthLabel().apply("  HELLO  ")` | `"Length: 5"` |
| names `Alice, Bob, Alexander, "", Amy, Barbara` filtered by `acceptedName()` | `Alice, Alexander, Amy, Barbara` |

Think about the list with no steps at all, and about a `null` name reaching the predicate.
