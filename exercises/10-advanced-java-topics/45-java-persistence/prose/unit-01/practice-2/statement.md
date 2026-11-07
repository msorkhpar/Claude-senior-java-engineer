The support screen shows a customer's open orders. Its old query method handed the screen a live
cursor, and the screen failed with `The object is already closed`. A quick rewrite fixed that, but
after a day under load the database refuses new statements on long-lived connections, and once a
request failed because the connection it was still using had been closed.

Write `openOrders(connection, customer)` in `src/main/java/practice/OrderQueries.java`. Read each
row with the existing `read(rs)`. When the method returns or throws, nothing it opened is left
open, and the connection it was lent is exactly as usable as before. Some old rows hold text such
as `a box` in the `quantity` column; `read` throws `SQLException` on them, and that exception
should reach the caller. About eight lines.

| customer | rows (id, item, quantity, status) | `openOrders` |
|---|---|---|
| `k-208` | 41 toner 85A 2 OPEN, 57 USB-C dock 1 SHIPPED, 63 paper A4 10 OPEN | `[OpenOrder[id=41, item=toner 85A, quantity=2], OpenOrder[id=63, item=paper A4, quantity=10]]` |
| `k-404` | none | `[]` |
