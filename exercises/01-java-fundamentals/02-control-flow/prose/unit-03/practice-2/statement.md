The course computes `n!` with a `do`-`while` loop that multiplies a `long` up. A `long`
holds `20!` but not `21!`, and plain `*` would wrap around silently.

Write `of(int n)` in `Factorial`:

- `of(0)` and `of(1)` are `1`, `of(5)` is `120`, and `of(20)` is `2432902008176640000`;
- a negative `n` has no factorial: throw `IllegalArgumentException`;
- when the result does not fit in a `long`, as for `of(21)`, throw `ArithmeticException`
  instead of returning a wrapped value. `Math.multiplyExact` does exactly that.
