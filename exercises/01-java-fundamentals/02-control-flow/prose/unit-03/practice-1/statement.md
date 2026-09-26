How many times a loop runs is not always known in advance: dividing a number by 10 until
nothing is left takes as many passes as the number has digits.

Write `count(long n)` in `Digits`. It returns how many decimal digits `n` has, ignoring
its sign:

- `count(7)` is `1`, `count(120)` is `3`, and `count(9876543210L)` is `10`;
- `0` is written with one digit, so `count(0)` is `1`;
- the sign is not a digit: `count(-120)` is `3`, and `count(Long.MIN_VALUE)` is `19`.

A `do`-`while` loop fits: its body runs once before the condition is checked.
