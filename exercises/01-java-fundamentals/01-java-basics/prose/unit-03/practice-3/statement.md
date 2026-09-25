Casting a `double` to an `int` **truncates**: `(int) 19.99` is `19`, and
`(int) -19.99` is `-19`. When the nearest whole number is wanted, the page's
advice is `Math.round`, which rounds a half up (toward positive infinity) and
returns a `long` for a `double`.

Write `Rounding.roundToWhole(double amount)`. It returns the nearest whole
number, with a half rounded up. A result that does not fit in an `int` is
refused with an `ArithmeticException`, never wrapped.

| amount | answer |
|---|---|
| `19.99` | `20` |
| `3.14` | `3` |
| `2.5` | `3` |
| `-2.5` | `-2` |
| `-19.99` | `-20` |
| `3.0e9` | `ArithmeticException` |
