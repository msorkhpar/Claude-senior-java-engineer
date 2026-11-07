Every morning the accounts team runs the reconciler over the bank's statement file. It matches
each payment to the open invoice whose reference the payer typed, and sends the rest to a person
for manual review. The team's tests pass, but in production almost every payment ends up in
manual review, the run sometimes dies halfway through, and once a payment with no reference at all
was booked against an invoice.

`readStatement` and `match` already work. Finish the `TODO` in
`src/main/java/practice/Reconciler.java`:

- `sameReference(invoiceRef, paymentRef)`: whether a payment with that reference pays an invoice
  issued with that one.

About two lines.
