Java 17 can close an exception hierarchy with `sealed`, and Java 21 can then
switch over it exhaustively. The page's payment family has two members; this
one has a third, `FraudSuspectedException`. Complete the family and describe it.

1. Make `PaymentException` a **sealed** abstract checked exception that permits
   exactly `CardDeclinedException`, `PaymentTimeoutException` and
   `FraudSuspectedException` (all `final`).
2. Write `PaymentDescriber.describe(PaymentException e)` with a pattern-matching
   `switch` over the family:

| argument | result |
|---|---|
| `new CardDeclinedException("insufficient limit")` | `"declined: insufficient limit"` |
| `new PaymentTimeoutException("gateway slow")` | `"retry later"` |
| `new FraudSuspectedException("velocity check")` | `"blocked"` |

When the family is sealed the compiler knows every subtype, so the switch
needs no `default` branch, and a member added later makes the switch fail to
compile instead of falling into the wrong branch.
