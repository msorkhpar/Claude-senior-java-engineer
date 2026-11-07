The returns service archives incoming orders on a small worker pool. Its support queue has three
complaints this week: a customer who submitted the same return twice has two archive entries; an
order hit a moment when the archive volume was busy and was never archived, although the
dispatcher is meant to retry such failures; and an order the archive refused every time was
reported as archived.

`OrderDispatch.dispatch(...)` already decides what to archive, retries and reports. It reads only
the `Future`s that two methods return, and those two are still `TODO`s in
`src/main/java/practice/OrderDispatch.java`:

- `remember(orderId)`: records the id on the pool; `dispatch` skips the order when the `Future`'s
  value is `false`;
- `store(orderId)`: stores the order with `archive.store(...)` on the pool; `dispatch` retries
  when the `Future` fails with an `IOException` as its cause, and treats a `Future` that ends
  normally as stored.

Keep the signatures: other callers depend on `Future<?>`. About four lines in total.

The tests run the dispatcher on a real `ExecutorService` with an archive that refuses some stores.
