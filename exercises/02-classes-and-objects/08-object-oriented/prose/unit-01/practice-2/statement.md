The payment service sends charges and refunds to the bank. A charge sent twice charges the
customer twice, so a `Payment` is tried once. A refund carries the key of the charge it reverses,
and the bank applies it once however often it arrives, so refunds implement the platform team's
`Idempotent` interface, which says how often an idempotent operation may be tried. The platform
team sets that number from its configuration and changes it when the bank's limits change.

Finish the two `TODO`s in `src/main/java/practice/Retries.java`:

- `send(...)`: try a payment until the bank accepts it or the payment has used its attempts;
- `Refund.maxAttempts()`: how many times a refund may be tried.

When you're done, charges are still sent once, and a failing refund is tried exactly as often as
the platform's policy allows at the time, today and after the platform changes it. About five
lines in total.
