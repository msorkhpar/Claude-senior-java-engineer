Overloaded methods share a name and differ in their parameter lists: in how many parameters
there are, in their types, or in their order. The compiler picks the one that fits the call.

Write five overloads in `Calculator`:

| call | result |
|---|---|
| `add(5, 10)` | `15`, an `int` |
| `add(5, 10, 15)` | `30`, an `int` |
| `add(5.5, 10.25)` | `15.75`, a `double` |
| `concat("x", 7)` | `"x7"`: the text first |
| `concat(7, "x")` | `"7x"`: the number first |
