The page's `DiscountStrategy` is the Strategy pattern as an enum: each constant
is built with a description and a `Function<Double, Double>` calculator. Its
pitfall is applying the calculator to raw input; the fix validates first and
cleans up the result:

```java
if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
return Math.max(0, Math.round(calculator.apply(amount) * 100.0) / 100.0);
```

The constants and their calculators are given (note that the flat ones
subtract without a floor). Write:

- `double applyDiscount(double amount)`: refuses a negative `amount` with
  `IllegalArgumentException`; otherwise applies the calculator, rounds to
  cents, and never returns less than `0`.
- `static DiscountStrategy bestDiscount(double amount)`: of every strategy
  except `NONE`, the one whose `applyDiscount(amount)` is lowest. If several
  tie, the one declared first wins.

| call | answer |
|---|---|
| `PERCENTAGE_20.applyDiscount(100)` | `80.0` |
| `PERCENTAGE_10.applyDiscount(19.99)` | `17.99` |
| `FLAT_10.applyDiscount(8)` | `0.0` |
| `NONE.applyDiscount(-1)` | throws `IllegalArgumentException` |
| `bestDiscount(100)` | `BUY_ONE_GET_HALF` |
| `bestDiscount(40)` | `FLAT_10` (ties with `BUY_ONE_GET_HALF`, declared later) |
