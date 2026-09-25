`Integer.parseInt` converts a `String` to an `int`, and throws a
`NumberFormatException` for text that is not an `int`, including a number too
large for one. A wrapper type can hold `null`, so an `Integer` can say "no number"
where an `int` cannot; and **unboxing** a `null` `Integer` into an `int` throws
a `NullPointerException`.

Write two methods in `Parsing`:

1. `Integer parseOrNull(String text)` returns the `int` the text spells, with
   surrounding spaces ignored, or `null` when it spells none (`null` text,
   an empty string, `"abc"`, `"12.5"`, a number beyond the `int` range).
2. `int parseOrDefault(String text, int fallback)` returns the same number, or
   `fallback` when there is none.

| call | answer |
|---|---|
| `parseOrNull("123")` | `123` |
| `parseOrNull(" 42 ")` | `42` |
| `parseOrNull("abc")` | `null` |
| `parseOrNull("2147483648")` | `null` |
| `parseOrDefault("oops", 5)` | `5` |
