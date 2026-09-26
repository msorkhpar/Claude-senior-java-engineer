Plain recursive Fibonacci is O(2^n): `fib(n - 1)` and `fib(n - 2)` recompute
the same values again and again. The page's pitfall on space shows the fix,
memoization: keep each result in a `Map` and **read the memo before
computing**, trading O(n) space for O(n) time.

Write `Fibonacci.fib(n, memo)` with `fib(0) = 0`, `fib(1) = 1`:

- `memo` is given by the caller and **kept between calls**: store every
  computed `fib(k)` in it with `put` and use what is already there. The tests
  count the `put` calls. (`computeIfAbsent` does not work here: a `HashMap`
  refuses to be changed by the function it is running.)
- `n < 0` is refused with `IllegalArgumentException`.
- **Overflow is refused, not wrapped**: a result that does not fit in a
  `long` throws `ArithmeticException`.

| call | result | memo puts |
|---|---|---|
| `fib(10, new memo)` | 55 | |
| `fib(50, new memo)` | 12586269025 | |
| `fib(90, new memo)` | 2880067194370816120 | |
| `fib(25, new memo)` | 75025 | at most 26 |
| `fib(41, memo after fib(40))` | 165580141 | 1 more |
| `fib(93, memo)` | `ArithmeticException` | |
