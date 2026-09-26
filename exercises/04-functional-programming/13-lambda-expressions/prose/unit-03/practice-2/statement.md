A functional interface has exactly one abstract method, but it may carry any number of
`default` and `static` methods. That is how a domain interface such as an order validator
earns its place over a plain `Predicate<Order>`: its name says what it checks, and its own
default methods let validators be combined.

`OrderValidator` declares the one abstract method `boolean validate(Order order)`, where
`Order` is the record `Order(List<String> items, double total, String customer)`. Write its
other methods:

1. `default OrderValidator and(OrderValidator other)`: valid when both are.
2. `default OrderValidator or(OrderValidator other)`: valid when at least one is.
3. `default OrderValidator negate()`: valid when this one is not.
4. `static OrderValidator hasItems()`: the order's item list is not empty, whatever its entries
   are and whatever the total.
5. `static OrderValidator hasCustomer()`: the order's customer is not `null` (a blank customer
   still counts).

| validator | order | answer |
|---|---|---|
| `hasItems().and(hasCustomer())` | `(["book"], 12.5, "c-1")` | `true` |
| `hasItems().and(hasCustomer())` | `(["pen"], 3.0, null)` | `false` |
| `hasItems().or(hasCustomer())` | `([], 0.0, "c-2")` | `true` |
| `hasItems().negate()` | `([], 0.0, null)` | `true` |

A validator may be expensive, or may only be safe to run after an earlier one has passed:
combine them the way `&&` and `||` combine conditions, not the way `&` and `|` do.
