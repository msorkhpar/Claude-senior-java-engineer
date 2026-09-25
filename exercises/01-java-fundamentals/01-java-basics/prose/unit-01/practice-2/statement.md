Write `ReverseDigits.reverse(int x)`. It returns `x` with its decimal digits in
reverse order, keeping the sign. Trailing zeros of `x` disappear, as they would
on paper.

| x | answer |
|---|---|
| `123` | `321` |
| `-123` | `-321` |
| `1200` | `21` |

Some reversals do not fit in an `int`: `1534236469` reversed is `9646324351`.
An `int` that overflows does not fail, it **wraps around silently**, so a
wrapped answer would look like a real one. When the reversed value does not fit
in an `int`, return `0` instead.
