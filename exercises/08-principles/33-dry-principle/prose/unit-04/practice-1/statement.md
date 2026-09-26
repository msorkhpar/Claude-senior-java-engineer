The page's key question is: "If one copy changes, must the other change too?"

- The bulk discount is **the same rule** in two contexts, online and in store. If one copy
  changes, the other must too, so the rule is written once:

  ```java
  double applyBulkDiscount(double price, int qty) {
      if (qty >= 100) return price * 0.80;
      if (qty >= 50)  return price * 0.90;
      if (qty >= 10)  return price * 0.95;
      return price;
  }
  ```

- Tax and discount only **look alike**. The page's over-DRY version merges them behind a
  flag, `genericCalculation(amount, rate, isTax)`. They are different business rules, so
  each keeps its own method, its own validation and its own error message.

Complete `Pricing`:

- `calculateTax(amount, taxRate)` is `amount * (1 + taxRate)`, and
  `calculateDiscount(amount, discountRate)` is `amount * (1 - discountRate)`. A negative
  amount throws `IllegalArgumentException("Amount must not be negative")`. A rate outside
  0 to 1 throws `IllegalArgumentException` naming its own rule:
  `"Tax rate must be between 0 and 1"` or `"Discount rate must be between 0 and 1"`. The
  rates `0` and `1` are both valid (the bounds are exact, and `NaN` is not among the inputs), and mean different things: a tax rate of 1 doubles the
  amount, and a discount rate of 1 makes it free;
- `applyBulkDiscount(price, quantity)` is the one bulk rule above: 5% off from 10 items,
  10% off from 50, 20% off from 100;
- the tests see results and messages, not how your methods are arranged, so keeping tax and discount in
  separate methods is the page's lesson rather than a graded step;
- `onlinePrice` and `inStorePrice` both apply that one rule, with no rounding and no rule of their own, so the two channels agree at
  every quantity, the tier boundaries included.

Examples:

- `calculateTax(100, 0.2)` is `120.0`, and `calculateDiscount(100, 0.2)` is `80.0`;
- `calculateTax(50, 1.0)` is `100.0`, and `calculateDiscount(50, 1.0)` is `0.0`;
- at a unit price of `10.0`, both channels charge `10.0` for 9 items, `9.5` for 10,
  `9.0` for 50 and `8.0` for 100;
- `calculateDiscount(100, -0.1)` throws with the message
  `"Discount rate must be between 0 and 1"`.
