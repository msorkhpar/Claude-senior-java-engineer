Records are a natural fit for **value objects**: an amount of money is defined
entirely by its value, two equal amounts are interchangeable, and it never
changes. Because a record's state cannot be modified after creation, an
operation returns a **new** value instead.

Complete `record Money(long cents, String currency)`:

- **compact constructor**: a `null` currency is refused with a
  `NullPointerException`;
- **`Money plus(Money other)`**: a new `Money` holding the sum, in the same
  currency. Adding a different currency is refused with an
  `IllegalArgumentException`, and a sum that does not fit in a `long` is
  refused with an `ArithmeticException` (`Math.addExact` helps). Neither
  operand changes.

## Examples

```
new Money(1050, "EUR").plus(new Money(225, "EUR"))    -> Money[cents=1275, currency=EUR]
new Money(1050, "EUR").plus(new Money(225, "USD"))    -> IllegalArgumentException
new Money(Long.MAX_VALUE, "EUR").plus(new Money(1, "EUR"))  -> ArithmeticException
new Money(100, null)                                  -> NullPointerException
```
