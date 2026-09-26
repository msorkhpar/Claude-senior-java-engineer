`int` arithmetic wraps silently: `Integer.MAX_VALUE + 1` is
`Integer.MIN_VALUE`, and a price times a quantity can come out small or
negative, which an attacker can use to pay less. The page's fix is
`Math.addExact` and `Math.multiplyExact`, which **throw `ArithmeticException`**
instead. Its edge cases add a trap: `-Integer.MIN_VALUE` and
`Math.abs(Integer.MIN_VALUE)` are both still `Integer.MIN_VALUE`.

Write `OrderMath` with three methods that throw `ArithmeticException` on any
overflow:

- `lineTotal(unitPriceCents, quantity)`: price times quantity.
- `total(lines...)`: the sum of the line totals, **checking every addition**.
- `distance(a, b)`: the absolute difference `|a - b|`.

| call | result |
|---|---|
| `lineTotal(1999, 3)` | `5997` |
| `total(5997, 1000)` | `6997` |
| `distance(3, 10)` | `7` |
| `lineTotal(65536, 65536)` | `ArithmeticException` (it would wrap to 0) |
| `total(Integer.MAX_VALUE, 1, -1)` | `ArithmeticException` |
| `distance(Integer.MIN_VALUE, 0)` | `ArithmeticException` |
