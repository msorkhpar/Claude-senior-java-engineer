A lambda is a parameter list, the arrow `->`, and a body. The body is either a
single expression, `(parameters) -> expression`, or a block of statements,
`(parameters) -> { statements; }`, which may declare local variables and branch.

Write two static methods in `PriceRules`, each mapping a lambda over the prices:

1. `discounted(List<Double> prices, double percent)` returns each price reduced
   by `percent` per cent, rounded to two decimal places (cents).
2. `categorize(List<Double> prices)` names each price's band: below 10 is
   `"cheap"`, below 100 `"moderate"`, below 1000 `"expensive"`, and anything
   else `"premium"`.

| call | answer |
|---|---|
| `discounted([100.0, 50.0], 10)` | `[90.0, 45.0]` |
| `discounted([19.99], 15)` | `[16.99]` |
| `categorize([5.0, 50.0, 500.0, 5000.0])` | `["cheap", "moderate", "expensive", "premium"]` |

Watch the prices that sit exactly on a limit, and what floating-point
arithmetic leaves after a percentage.
