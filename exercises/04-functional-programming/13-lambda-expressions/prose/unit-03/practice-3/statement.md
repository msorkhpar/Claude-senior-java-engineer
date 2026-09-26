Functional interfaces compose. `f.andThen(g)` runs `f` and then `g`, which is `g(f(x))`;
`f.compose(g)` runs `g` first, which is `f(g(x))`. Predicates combine with `and`, `or` and
`negate`, and `and` stops at the first `false`.

Write three static methods of `Compositions`:

1. `<T> Function<T, T> inOrder(List<Function<T, T>> steps)` returns one function that applies
   every step, first to last.
2. `Function<String, String> lengthLabel()` trims, lower-cases, measures the length, and
   formats it as `"Length: n"`.
3. `Predicate<String> acceptedName()` accepts a name that is not `null`, not empty, and either
   starts with `"A"` or is longer than 5 characters.

| call | answer |
|---|---|
| `inOrder(List.of(x -> x + 1)).apply(1)` | `2` |
| `inOrder(List.of(x -> x + 1, x -> x * 2)).apply(3)` | `8` |
| `lengthLabel().apply("  HELLO  ")` | `"Length: 5"` |
| names `Alice, Bob, Alexander, "", Amy, Barbara` filtered by `acceptedName()` | `Alice, Alexander, Amy, Barbara` |

Think about the list with no steps at all, and about a `null` name reaching the predicate.
