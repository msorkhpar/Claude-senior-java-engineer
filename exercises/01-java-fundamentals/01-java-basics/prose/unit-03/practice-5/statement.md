For primitives, `int` widens to `long` implicitly. For their wrappers it does
not: an `Integer` is not a `Long`, so `(Long) someIntegerObject` compiles and then
throws a `ClassCastException` at run time. Casting between incompatible reference
types is a run-time failure the compiler cannot catch.

Write `WholeNumbers.toLong(Object value)`. It returns the value of a `Byte`,
`Short`, `Integer` or `Long` as a `long`. Anything else, including `null`, a
`Double` and a `String`, is refused with an `IllegalArgumentException`, never a
`ClassCastException`.

| value | answer |
|---|---|
| `5L` | `5` |
| `Integer.valueOf(5)` | `5` |
| `(short) 7` | `7` |
| `1.5` (a `Double`) | `IllegalArgumentException` |
| `"7"` | `IllegalArgumentException` |
