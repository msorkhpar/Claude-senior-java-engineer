The lesson's real-world abstraction: a `PaymentProcessor` interface, with a
credit-card and a PayPal implementation. The rest of the application works
with the interface and never needs to know how each method is processed.

`Payments.PaymentProcessor` is the lesson's interface with one query added:

```
boolean processPayment(double amount);  // true when the payment is accepted
void refund(double amount);
double charged();                        // what has been charged and not refunded
```

Write the two implementations and one method written only against the
interface:

- `CreditCardProcessor(double limit)` accepts a payment when the amount is
  positive and the charges, with it, stay **at or below** the limit.
- `PayPalProcessor(double balance)` accepts a payment when the amount is
  positive and **at most** the balance left; the balance goes down by it.
- Either processor refuses (returns `false`, charges nothing) a zero or
  negative amount.
- `refund(amount)` lowers the charges (PayPal also puts the amount back on
  the balance). Refunding more than has been charged throws
  `IllegalArgumentException` and changes nothing.
- `Payments.payAll(PaymentProcessor processor, double... amounts)` tries each
  amount in order and returns how many were accepted.

## Examples

```
card = new CreditCardProcessor(100)
payAll(card, 30, 50, 40)  -> 2        card.charged() -> 80.0
payAll(card, 20)          -> 1        card.charged() -> 100.0   (exactly the limit)
paypal = new PayPalProcessor(60)
payAll(paypal, 20, 50)    -> 1        paypal.charged() -> 20.0
card.processPayment(0)    -> false
new CreditCardProcessor(100).refund(10) -> IllegalArgumentException
```
