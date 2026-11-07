At the end of the day, the payment service reconciles: it asks the bank for the status of every
payment of the day. Each lookup blocks until the bank answers, which can take seconds. Last week
someone made the lookups parallel, and since then the card authorisations that the same service
answers time out for as long as the reconciliation runs. The authorisations use parallel streams
and `CompletableFuture`'s async methods without an executor.

Finish `statuses(...)` in `src/main/java/practice/StatusReport.java`. When you're done:

- up to `lookupsAtOnce` lookups wait on the bank at the same time, on threads that belong to the
  report, and the statuses come back in the order of the ids;
- when the report returns, or fails, the threads it used end;
- a lookup that fails with an unchecked exception makes `statuses` throw an exception of the same
  type.

About twelve lines in total.

The tests use a bank that answers only when several lookups are waiting on it, and one that fails
for a single payment.
