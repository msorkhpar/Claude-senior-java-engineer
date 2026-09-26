The page's Optional rules: return `Optional` when a result may be absent, build
it with `Optional.ofNullable` (`Optional.of(null)` throws), and chain
`map`, `flatMap`, `filter` and `orElse` instead of nesting null checks. Use
`flatMap` when the next step itself returns an `Optional`, so you never get an
`Optional<Optional<T>>`.

Write the class `Directory`, built from two maps: user id to name, and name to
city. Either map may lack a key.

- `Optional<String> name(int id)`: the user's name, or empty.
- `Optional<String> city(int id)`: the city of the user's name, or empty when
  there is no user **or** no city for that name.
- `String badge(int id)`: the name upper-cased with **`Locale.ROOT`** (never the
  machine's default locale) when it is longer than 3 characters, so `"Dave"` gets
  `"DAVE"`; otherwise, or with no user, `"GUEST"`.

| names | cities | call | answer |
|---|---|---|---|
| `{1: "Alice", 2: "Bob"}` | `{"Alice": "Paris"}` | `name(1)`, `city(1)`, `badge(1)` | `Optional[Alice]`, `Optional[Paris]`, `"ALICE"` |
| same | same | `city(2)` | `Optional.empty` |
| same | same | `badge(2)` | `"GUEST"` |
| same | same | `name(9)`, `city(9)`, `badge(9)` | `Optional.empty`, `Optional.empty`, `"GUEST"` |
