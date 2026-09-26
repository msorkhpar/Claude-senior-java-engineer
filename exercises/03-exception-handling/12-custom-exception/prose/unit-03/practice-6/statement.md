Exception messages end up in logs and sometimes in responses, so they must
not hold passwords, tokens or full card numbers. They should still say what
failed, with which values. Write:

1. The checked `CardDeclinedException(String message, String lastFour)` with
   `getLastFour()`.
2. `Payments.authorize(String cardNumber, long amountCents, long limitCents)`:
   returns `"approved"` when the amount is within the limit, and otherwise
   throws `CardDeclinedException` with the message
   `Card ending <last four> declined: <amount> over limit <limit>` and the last
   four digits.

| call | result |
|---|---|
| `authorize("4111111111111111", 2000, 3000)` | `"approved"` |
| `authorize("4111111111111111", 5000, 3000)` | throws `CardDeclinedException("Card ending 1111 declined: 5000 over limit 3000")`, `getLastFour()` is `"1111"` |

Card numbers are at least four digits long here. Be precise about what "within
the limit" means.
