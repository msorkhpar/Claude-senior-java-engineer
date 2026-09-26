The page implements the Strategy pattern with an enum whose constants each
receive a `UnaryOperator<String>` in their constructor: the behavior is a
lambda passed as a constructor argument, not a method body.

The starter builds every constant with a placeholder operator. Replace each
placeholder with the operator it names, and write `apply` and `applyAll`:

- `UPPER_CASE`, `LOWER_CASE`: change case using `Locale.ROOT`, so the answer
  does not depend on the machine's default locale.
- `TRIM`: remove leading and trailing whitespace.
- `REVERSE`: the characters in reverse order.
- `CAPITALIZE`: the first character upper case and the rest lower case
  (`Locale.ROOT` again); the empty text stays empty.
- `SNAKE_CASE`: trim, turn each run of whitespace into one `_`, lower case.
- `String apply(String input)`: apply the constant's operator; a `null` input
  throws `IllegalArgumentException`.
- `static String applyAll(String input, TextTransform... transforms)`: apply
  the transforms one after another, from left to right.

| call | answer |
|---|---|
| `REVERSE.apply("abc")` | `"cba"` |
| `CAPITALIZE.apply("hELLO")` | `"Hello"` |
| `SNAKE_CASE.apply("  Hello   big World ")` | `"hello_big_world"` |
| `applyAll("  hello WORLD  ", TRIM, CAPITALIZE)` | `"Hello world"` |
| `UPPER_CASE.apply("title")` under a Turkish default locale | `"TITLE"` |
