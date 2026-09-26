The page balances DRY with SRP by layering. Primitive checks are truly cross-cutting, so
they are shared; each domain validator uses them, and owns only its domain's rules:

```java
class ValidationUtils {
    static boolean isNonBlank(String s) { return s != null && !s.isBlank(); }
    static boolean isPositive(Number n) { return n != null && n.doubleValue() > 0; }
}
```

Complete `Validation`:

- `isNonBlank(value)`, `isPositive(value)` and `isInRange(value, min, max)` are the shared
  checks. **For `null` they return `false` rather than throwing**, and `isInRange`
  **includes both ends** of its range, exactly (no tolerance). `isPositive` is the page's
  `n.doubleValue() > 0`, so `NaN` is not positive. The maximum quantity, 10000, is itself allowed;
- `validateOrder(customerId, amount, quantity)` returns every problem it finds, in this
  order: `"Customer ID is required"` (a null or blank id), `"Amount must be positive"`,
  `"Quantity must be positive"`, and `"Quantity exceeds maximum allowed (10000)"`;
- `validateEmployee(name, age, salary)` returns every problem, in this order:
  `"Name is required"`, `"Age must be between 18 and 120"` and `"Salary must be positive"`;
- an empty list means the input is valid, and a validator **lists every problem**, not
  just the first.

Examples:

- `validateOrder("c-1001", 25.0, 3)` is empty, and `validateOrder("", 25.0, 3)` is
  `["Customer ID is required"]`;
- `validateEmployee(null, 30, 5000)` is `["Name is required"]`, with no exception;
- ages `18` and `120` are valid, and `17` and `121` are not;
- `validateOrder("   ", -5, 20000)` lists all three of its problems.
