Catch blocks are tried from top to bottom, so a subclass must be caught before its superclass. The
types in one multi-catch must be unrelated.

Write `HundredDivider.describe(String input)`. It parses `input` as an `int` and divides `100` by it
with integer division. It returns:

| situation | returns |
|---|---|
| the division succeeds | `"Result: " + quotient` |
| `input` is not a number (a `NumberFormatException`) | `"Not a number: " + message` |
| the value is negative | `"Invalid input: Value must be non-negative"` |
| the division throws an `ArithmeticException` | `"Invalid input: " + message` |

| call | answer |
|---|---|
| `describe("4")` | `"Result: 25"` |
| `describe("3")` | `"Result: 33"` |
| `describe("-5")` | `"Invalid input: Value must be non-negative"` |
| `describe("abc")` | `"Not a number: For input string: \"abc\""` |

`NumberFormatException` is itself an `IllegalArgumentException`. And not every kind of division
throws when the divisor is zero.
