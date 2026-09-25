Widening is implicit and never loses **magnitude**, but `int` to `float` can lose
**precision**: a `float` keeps 24 significant bits, so
`float f = 123456789;` stores `1.23456792E8`.

Write `FloatPrecision.fitsInFloat(int value)`. It returns `true` when converting
`value` to `float` keeps it exactly, and `false` when the conversion rounds it.

| value | answer |
|---|---|
| `100` | `true` |
| `123456789` | `false` |
| `16777216` (2^24) | `true` |
| `16777217` | `false` |

Two traps:

- Some large values still fit: a `float` can hold an even number just above
  2^24 exactly, so a rule based only on size is wrong.
- Checking the result by comparing `float` with `int` promotes the `int` to
  `float` first, and casting the `float` back to `int` clamps at the `int` range.
  Compare in a type that holds both exactly.
