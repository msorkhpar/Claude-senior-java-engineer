`NaN` ("not a number") is what a failed floating-point computation produces.
Every relational operator with a `NaN` operand is `false`, even `NaN == NaN`,
so the page's advice is to test for it with `Double.isNaN`. And beware of
`Double.MIN_VALUE`: it is the smallest **positive** double, not the most negative one.

A sensor writes one `double` per reading, and writes `NaN` when a reading failed.
Write `Readings.largest(double[] readings)`. It returns the largest real reading,
ignoring every `NaN`. When there is no real reading (an empty array, or only
`NaN`s) it returns `NaN`.

| readings | answer |
|---|---|
| `{1.5, 3.25, 2.0}` | `3.25` |
| `{-5.0, -3.0, -9.0}` | `-3.0` |
| `{1.0, NaN, 4.0}` | `4.0` |
| `{}` | `NaN` |
