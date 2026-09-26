A class with many optional parameters ends up with a tangle of overloaded
constructors. The page's cure is the **Builder pattern**: the required values go to
the builder's constructor, each optional one has its own method, and `build()`
checks everything once and creates the object.

Write `Order` and its nested static class `Order.Builder`:

- `Order.builder(String customer)` returns a builder. The customer is required:
  `null` throws `NullPointerException`.
- `quantity(int)`, `note(String)` and `express(boolean)` each set one option and
  return the same builder, so calls can be chained.
- Defaults: quantity `1`, note `""` (empty), express `false`.
- `build()` returns the `Order`, and refuses a quantity below `1` with
  `IllegalArgumentException`.
- `Order` has the getters `getCustomer()`, `getQuantity()`, `getNote()` and
  `isExpress()`, and no public constructor.

**Examples**

```
Order.builder("Ann").quantity(3).note("ring twice").express(true).build()
    -> Ann, 3, "ring twice", express
Order.builder("Ann").build()           -> Ann, 1, "", not express
Order.builder("Ann").quantity(0).build() -> IllegalArgumentException
```
