A marketplace pays out the refunds its partners report. The partner feed gives each amount in
euros as a `double`, and the bank wants whole cents. Customers complain that some refunds arrive
a cent short, reversals sometimes take back a cent too little, and last month a partner's broken
export produced a batch of payouts for 0 cents that nobody noticed until the customers called.

`RefundBatch.payouts` already builds the instructions. Finish the `TODO` in
`src/main/java/practice/RefundBatch.java`: `toCents(euros)` returns the amount the feed means, to
the nearest cent, for positive and negative amounts alike, and stops the batch with an
`IllegalArgumentException` when the amount isn't a usable number.

About two lines.
