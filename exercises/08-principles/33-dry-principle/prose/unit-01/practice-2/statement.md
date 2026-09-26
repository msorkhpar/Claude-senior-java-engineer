The page's magic-number violation repeats the same rate in two calculations:

```java
public double calculateShippingCost(double weight) {
    return weight * 0.15; // What is 0.15?
}
public double estimateDelivery(double weight) {
    return weight * 0.15 + 5.0; // Same magic number!
}
```

Its fix names the rate once (`COST_PER_KG`) and has both calculations read it. Here the
rates are the fields of `ShippingRates`, so each rate lives in exactly one place. Complete
the two prices:

- `shippingCost(weightKg)` is exactly `weightKg * costPerKg`, with no rounding. Any
  negative weight, however small (`-0.001`), throws `IllegalArgumentException`;
- `deliveryEstimate(weightKg)` is the shipping cost plus `baseDeliveryFee`, for every
  weight: it reads the same `costPerKg`, and refuses a negative weight exactly as
  `shippingCost` does. Reusing `shippingCost` gives both for free, where a repeated
  formula is one more copy to keep in step.

Examples, with `ShippingRates.standard()` (0.15 per kilogram, fee 5.0):

- `shippingCost(10)` is `1.5` and `deliveryEstimate(10)` is `6.5`;
- a zero weight costs `0.0` to ship, and its delivery estimate is the fee, `5.0`;
- `shippingCost(0.01)` is `0.0015`, not rounded to cents;
- with `new ShippingRates(0.40, 2.0)`, `shippingCost(10)` is `4.0` and
  `deliveryEstimate(10)` is `6.0`;
- `shippingCost(-1)`, `deliveryEstimate(-1)` and `deliveryEstimate(-0.001)` all throw
  `IllegalArgumentException`.
