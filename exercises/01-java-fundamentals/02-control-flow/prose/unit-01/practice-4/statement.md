The ternary operator `condition ? a : b` is a shorthand for an `if`/`else` that chooses
between two values: `(age >= 18) ? "Adult" : "Minor"`.

Write two methods in `Largest`:

1. `max(int a, int b, int c)` returns the largest of three values:
   `max(5, 3, 1)`, `max(1, 5, 3)` and `max(3, 1, 5)` are all `5`, and `max(5, 5, 5)` is `5`.
2. `status(int age)` returns `"Adult"` from `18` up and `"Minor"` below it:
   `status(20)` is `"Adult"`, `status(18)` is `"Adult"`, `status(17)` is `"Minor"`.

Every `int` is allowed, negative ones included.
