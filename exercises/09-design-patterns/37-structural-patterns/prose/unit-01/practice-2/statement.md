The page's Q1 adapts a legacy payment processor that charges **whole cents**
to a modern gateway that takes a `double` amount in dollars. The adapter only
translates: it converts the arguments, calls the legacy method and converts
the answer back.

Given (do not change them): `ModernPaymentGateway`, `PaymentResult` and
`LegacyPaymentProcessor`, whose `charge(card, amountInCents)` returns a status
code, `0` meaning success. Write `PaymentAdapter implements ModernPaymentGateway`:

- `new PaymentAdapter(legacy)` keeps the processor; **`null` is refused at
  once** with `NullPointerException`.
- `processPayment(card, amount)` charges the same card with the amount in cents,
  **rounded to the nearest cent** (the page warns that `(int) (19.99 * 100)` is
  `1998`).
- It returns `new PaymentResult(success, statusCode)`, where **only status `0`
  is a success**.

| legacy returns | call | charged | result |
|---|---|---|---|
| `0` | `processPayment("4111", 12.50)` | `("4111", 1250)` | `PaymentResult[success=true, statusCode=0]` |
| `0` | `processPayment("4111", 19.99)` | `("4111", 1999)` | `PaymentResult[success=true, statusCode=0]` |
| `0` | `processPayment("4111", 10.004)` | `("4111", 1000)` | `PaymentResult[success=true, statusCode=0]` |
| `51` | `processPayment("4111", 5.00)` | `("4111", 500)` | `PaymentResult[success=false, statusCode=51]` |
| `-1` | `processPayment("4111", 1.00)` | `("4111", 100)` | `PaymentResult[success=false, statusCode=-1]` |
| - | `new PaymentAdapter(null)` | - | `NullPointerException` |
