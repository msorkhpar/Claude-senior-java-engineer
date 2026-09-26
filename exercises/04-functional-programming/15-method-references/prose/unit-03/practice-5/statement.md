`f.andThen(g)` applies `f` first and then `g`; `f.compose(g)` applies `g` first and then
`f`. A method reference has no type until it gets a target, so to call `andThen` on
`String::trim` you first give it one, for example by assigning it to a
`Function<String, String>` variable. A two-argument unbound reference such as
`String::concat` fits a `BiFunction`, whose `andThen` takes a `Function`.

Write three factories in `Slugs`:

1. `Function<String, String> slug()` trims the text, lower-cases it and replaces each run
   of whitespace with one `-`.
2. `Function<String, Integer> slugLength()` gives the length of the slug of its argument.
   Build it as `String::length` **composed** with `slug()`.
3. `BiFunction<String, String, Integer> joinedLength()` gives the length of the two
   strings joined: `String::concat` and then `String::length`.

| call | result |
|---|---|
| `slug().apply("Hello World")` | `"hello-world"` |
| `slug().apply("  Hello World ")` | `"hello-world"` |
| `slugLength().apply("Hello World")` | `11` |
| `joinedLength().apply("Hello", "World")` | `10` |

The order of the steps matters, and titles are not always spaced neatly.
