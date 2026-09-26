Java 17 can close an exception hierarchy with `sealed`, and Java 21 can then
switch over it exhaustively. The page's payment family has two members; this
one has a third, `FraudSuspectedException`. Complete the family and describe it.

1. Make `PaymentException` a **sealed** abstract checked exception that permits
   exactly `CardDeclinedException`, `PaymentTimeoutException` and
   `FraudSuspectedException` (all `final`).
2. Write `PaymentDescriber.describe(PaymentException e)`:

| argument | result |
|---|---|
| `new CardDeclinedException("insufficient limit")` | `"declined: insufficient limit"` |
| `new PaymentTimeoutException("gateway slow")` | `"retry later"` |
| `new FraudSuspectedException("velocity check")` | `"blocked"` |

The tests grade the sealed family and the three results. The idiomatic body is
a pattern-matching `switch` with one case per member and no `default` branch:
because the family is sealed the compiler knows every subtype, and a member
added later makes such a switch fail to compile instead of falling into the
wrong branch. The tests cannot see which form your method takes.
