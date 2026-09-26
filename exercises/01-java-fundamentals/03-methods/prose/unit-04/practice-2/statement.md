When no overload takes exactly the argument's type, Java widens it: an `int` can go to a
`double` parameter. So if an `int` overload is missing, an `int` argument quietly reaches
the `double` one, and the program compiles, but means something else.

Write three `area` overloads in `Areas`:

| call | shape | result |
|---|---|---|
| `area(4)` | a square of side 4 (an `int`) | `16.0` |
| `area(1.0)` | a circle of radius 1.0 (a `double`) | `Math.PI` |
| `area(2.0, 3.5)` | a rectangle | `7.0` |

An `Integer` argument unboxes to `int`, so `area(Integer.valueOf(4))` is a square too.
