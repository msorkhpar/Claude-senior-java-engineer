A service should not leak its data layer's `SQLException`. Translate it at the
boundary into an exception of the service's own level of abstraction, keeping
it as the cause.

1. Complete the checked `OrderLookupException(String message, Throwable cause)`.
2. Write `OrderService(OrderDao dao)` and
   `String findOrder(long id) throws OrderLookupException`, which returns
   `dao.load(id)`. When `load` throws `SQLException`, throw
   `OrderLookupException("Could not load order <id>")`.

| DAO behaviour for `42` | `findOrder(42)` |
|---|---|
| returns `"order-42"` | `"order-42"` |
| throws `SQLException("connection reset")` | throws `OrderLookupException("Could not load order 42")` |

A caller depends only on the service's API, yet an operator reading the log
still needs to see `Caused by: java.sql.SQLException`. Catch only what you can
translate.
