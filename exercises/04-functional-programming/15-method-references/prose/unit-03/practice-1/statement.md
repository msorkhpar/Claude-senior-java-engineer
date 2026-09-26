`Consumer.andThen` chains consumers: `first.andThen(second)` hands each value to
`first`, then to `second`. A bound reference such as `processed::add` is a
`Consumer` that stores every value into that one list.

`Order` is a record `(String id, double amount, String customer)`. Write two methods in
`OrderDesk`:

1. `Consumer<Order> pipeline(List<String> auditLog, List<Order> processed)` returns one
   consumer that, for each order, first adds the line `"PROCESSED: <id> for <customer>"`
   to `auditLog`, then adds the order to `processed`.
2. `void processLarge(List<Order> orders, double threshold, Consumer<Order> pipeline)`
   feeds `pipeline` every order whose amount is **above** `threshold`, in order.

With orders `O1` (150.0, Alice), `O2` (75.0, Bob) and `O3` (200.0, Charlie) and a
threshold of `100`:

| list | contents afterwards |
|---|---|
| `auditLog` | `["PROCESSED: O1 for Alice", "PROCESSED: O3 for Charlie"]` |
| `processed` | the orders `O1` and `O3` |

The audit line for an order must already be written when that order is stored.
