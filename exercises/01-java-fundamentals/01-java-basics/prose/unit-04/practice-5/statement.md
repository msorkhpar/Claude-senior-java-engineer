`&&` and `||` **short-circuit**: when the left operand already decides the answer,
the right one is never evaluated. That is what makes a guard like
`s != null && s.length() > 0` safe. The non-short-circuit `&` and `|` evaluate
both sides every time. On booleans, `^` is true when the operands differ.

Write two methods in `Guards`:

1. `startsWithDigit(String text)` returns whether the first character of `text`
   is a digit. `null` and the empty string return `false` and never throw.
2. `exactlyOne(boolean a, boolean b)` returns `true` when exactly one of the two
   is `true`.

| call | answer |
|---|---|
| `startsWithDigit("7up")` | `true` |
| `startsWithDigit("abc")` | `false` |
| `startsWithDigit(null)` | `false` |
| `exactlyOne(true, false)` | `true` |
| `exactlyOne(true, true)` | `false` |
