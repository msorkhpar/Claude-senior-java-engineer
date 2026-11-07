The wallet service takes card payments in two steps: it debits the wallet, then inserts a payment
row whose `reference` column holds at most 16 characters. Support has three tickets this week. A
merchant sent a 19-character invoice number, the payment failed, and the customer's wallet was
short by the amount anyway. A payment request with a mistyped wallet id "succeeded" and left a
payment row for a wallet that doesn't exist. And after a failed payment, a later request on the
same pooled connection saved a profile change that no other server ever saw.

Write `pay(connection, wallet, amount, reference)` in `src/main/java/practice/Payments.java`, using
the existing `debit(...)` and `record(...)`. A payment either debits and records, or changes
nothing and throws `SQLException`. The connection is lent by the request handler, which goes on
using it afterwards in the mode it had before the call. About twelve lines.

| wallet (balance) | `pay(...)` | afterwards |
|---|---|---|
| 311 (1200.50) | `pay(c, 311, 19.99, "ord-5512")` | balance 1180.51, one payment row |
| 311 (1200.50) | `pay(c, 311, 45.00, "invoice-2024-000731")` | `SQLException`; balance 1200.50, no payment row |
| none | `pay(c, 999, 10.00, "ord-5516")` | `SQLException`; no payment row |
