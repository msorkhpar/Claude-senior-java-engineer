When the set of strategies is fixed, the page makes the strategy interface
`sealed` and each strategy a record that carries its own data. A `switch`
over the sealed interface then needs no `default`: the compiler knows every
case.

Complete `Payments`. `PaymentStrategy` is sealed and permits three records;
`PaymentResult(boolean success, String message)` is given.

- Each record's compact constructor **refuses a blank detail** (`null`,
  empty or only spaces) with `IllegalArgumentException`: `CreditCard`'s
  `cardNumber` and `expiryDate`, `PayPal`'s `email`, `Crypto`'s `wallet`.
- `pay(amount)` **pays only an amount above zero**; otherwise it returns
  `PaymentResult(false, "Amount must be positive")`.
- A successful message shows the amount with two decimals, **written with
  `Locale.ROOT`** so it reads the same on every machine.
- `Payments.describe(strategy)` uses a `switch` with one case per record.

| strategy | call | answer |
|---|---|---|
| `CreditCard("0000111122224242", "12/30")` | `pay(12.5)` | `(true, "Paid 12.50 via credit card ending in 4242")` |
| `PayPal("buyer@example.org")` | `pay(3)` | `(true, "Paid 3.00 via PayPal (buyer@example.org)")` |
| `Crypto("wallet-01")` | `pay(7.25)` | `(true, "Paid 7.25 via crypto wallet")` |
| any | `pay(0)` | `(false, "Amount must be positive")` |
| `CreditCard("0000111122224242", "12/30")` | `describe` | `"Credit card ending in 4242"` |
| `PayPal("buyer@example.org")` | `describe` | `"PayPal account: buyer@example.org"` |
| `Crypto("wallet-01")` | `describe` | `"Crypto wallet: wallet-01"` |
| `PayPal("  ")` | build | `IllegalArgumentException` |
