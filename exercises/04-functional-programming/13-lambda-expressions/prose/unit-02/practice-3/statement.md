A lambda's parameter list takes several shapes: `()` for none, a bare name for
one untyped parameter, `(a, b)` for several. Three or more parameters need a
functional interface of your own, and a varargs method works as it does in any
method. Each factory's declared return type is the lambda's target type, so the
compiler infers the parameter types.

Write five factories in `LambdaShapes`, each returning a lambda. `TriFunction`
and `IntSummer` are given, nested in `LambdaShapes`.

1. `greeting()`: a `Supplier<String>` giving `"Hello, World!"`.
2. `length()`: a `Function<String, Integer>` giving `String.length()`, the
   number of UTF-16 `char`s (an emoji such as `"\uD83D\uDE00"` counts as 2).
3. `sum()`: a `BinaryOperator<Integer>` adding its two arguments.
4. `repeater()`: a `TriFunction<String, Integer, Boolean, String>` taking
   `(text, count, upper)`; it repeats `text` `count` times (any count from 0 up) and upper-cases the
   result only when `upper` is true.
5. `summer()`: an `IntSummer`, whose method is `int sum(int... numbers)`, adding
   all its arguments. The tests keep every sum within the range of an `int`.

| call | answer |
|---|---|
| `greeting().get()` | `"Hello, World!"` |
| `length().apply("hello")` | `5` |
| `sum().apply(2, 3)` | `5` |
| `repeater().apply("ab", 3, true)` | `"ABABAB"` |
| `summer().sum(1, 2, 3, 4, 5)` | `15` |

Mind the smallest calls each lambda can receive, and the case of `upper = false`.
