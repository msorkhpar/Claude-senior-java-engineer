In the page's `MathOperation`, the enum declares `abstract double apply(double
a, double b)` and every constant supplies its own body. Two consequences the
page points out:

- Each constant must handle its own edge cases. `DIVIDE` **and** `MODULUS`
  throw `ArithmeticException("Division by zero")` for a zero divisor (plain
  `double` arithmetic would quietly give `Infinity` or `NaN`).
- A constant with a body is an instance of an anonymous subclass, so its
  `getClass()` is not the enum. `getDeclaringClass()` is.

Complete `MathOperation`:

- Fill in each constant's `apply` body: `ADD` `+`, `SUBTRACT` `-`, `MULTIPLY`
  `*`, `DIVIDE` `/`, `MODULUS` `%`.
- `static Optional<MathOperation> fromSymbol(String symbol)`: the constant whose
  symbol equals `symbol`, or empty.
- `static String qualifiedName(Enum<?> constant)`: the simple name of the
  constant's enum type, a dot, and the constant's name, such as
  `"MathOperation.ADD"` or `"Precedence.HIGH"`.

| call | answer |
|---|---|
| `DIVIDE.apply(7, 2)` | `3.5` |
| `MODULUS.apply(7, 3)` | `1.0` |
| `MODULUS.apply(7, 0)` | throws `ArithmeticException` |
| `fromSymbol("*")` | `Optional[MULTIPLY]` |
| `qualifiedName(Precedence.HIGH)` | `"Precedence.HIGH"` |
| `qualifiedName(ADD)` | `"MathOperation.ADD"` |

A symbol usually arrives from parsed input, so it is not the same `String`
object as the constant's symbol.
