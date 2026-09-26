A varargs parameter, written `int... numbers`, accepts any number of arguments, none
included, and the method sees them as an array. It must be the last parameter, so a method
that needs at least one value takes that one first: `max(int first, int... rest)`.

Write two methods in `Varargs`:

1. `sum(int... numbers)` returns the total: `sum(1, 2, 3)` is `6`, `sum(10, 20)` is `30`,
   and `sum()` is `0`. An explicit `null` array counts as no numbers:
   `sum((int[]) null)` is `0`.
2. `max(int first, int... rest)` returns the largest of all its arguments: `max(3, 9, 4)`
   is `9`, `max(5)` is `5`, and `max(-2, -7)` is `-2`.
