A guard narrows a type pattern: `case Integer i when i < 0 ->`. The cases are tried from the
top, and the first one that matches wins, so when two guarded cases overlap, the narrower one
comes first. An unguarded `case Integer i` then catches every remaining `Integer`.

Write `classify(Object obj)` in `Classifier` as one switch expression, after the page's own
example:

| obj | result |
|---|---|
| an `Integer` below 0 | `"Negative integer"` |
| `0` | `"Zero"` |
| an `Integer` from 1 to 100 | `"Small positive integer"` |
| any larger `Integer` | `"Large positive integer"` |
| `Double.NaN` | `"Not a number"` |
| an infinite `Double` | `"Infinite"` |
| any other `Double` | `"Finite double"` |
| anything else | `"Not a number type"` |
