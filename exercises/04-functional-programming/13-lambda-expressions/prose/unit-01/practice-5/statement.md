A functional interface has exactly one abstract method; it may also carry
default and static methods. A lambda supplies the body of that one abstract
method, and the compiler uses its signature to type the lambda.

The interface `Calculator` is given, in the same file: its single abstract
method is `int calculate(int a, int b)`, with a default `square(int n)` and a
static `add(int a, int b)`.

Write `Calculators.of(char operator)`, which returns a `Calculator` lambda for
`'+'`, `'-'`, `'*'` or `'/'`. Division is Java's `int` division `a / b`, which
rounds toward zero, so `-7 / 2` is `-3`. Any other character, `'x'` and `':'` included, throws
`IllegalArgumentException` with the message `unknown operator: <operator>`,
for example `unknown operator: %`.

| call | answer |
|---|---|
| `of('+').calculate(5, 3)` | `8` |
| `of('*').calculate(5, 3)` | `15` |
| `of('-').calculate(5, 3)` | `2` |
| `of('/').calculate(7, 2)` | `3` |
| `of('+').square(5)` | `25` |

Consider *when* a bad operator should be reported, and what dividing by zero
must do.
