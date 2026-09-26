A switch expression is a value: it can be returned directly, and every arm gives one.

Write `say(int n)` in `FizzBuzz` as a single `return switch (...) { ... };`. It returns:

- `"FizzBuzz"` when `n` is divisible by both 3 and 5;
- `"Fizz"` when `n` is divisible by 3 only;
- `"Buzz"` when `n` is divisible by 5 only;
- `n` itself as text otherwise.

Examples: `say(15)` is `"FizzBuzz"`, `say(9)` is `"Fizz"`, `say(10)` is `"Buzz"`,
`say(7)` is `"7"`. The rules hold for every `int`: `say(0)` is `"FizzBuzz"`, `say(-3)`
is `"Fizz"` and `say(-7)` is `"-7"`.

Hint: switch on the remainder by 15, and list several remainders in one arm.
