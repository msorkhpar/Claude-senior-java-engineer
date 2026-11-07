Wallet transfers run all day; at midnight an interest job adds a little to every wallet, one
transaction per batch, in its own order. Since the job moved earlier, a few transfers a night fail
with SQLState 40001. Customers retry by hand, and support would like that to stop. A first attempt
at an automatic retry made the nightly totals stop adding up, and a transfer to a wallet that is
already near its limit now hammers the database three times before failing.

Finish `transfer(...)` in `src/main/java/practice/Transfers.java`, using `debit` and `credit`. The
table's `CHECK` keeps every balance between 0 and 1000. When you're done, a transfer that collides
with the interest job still completes with the right balances, and a transfer the database refuses
for its own rules fails at once and changes nothing. About twelve lines.

The tests run on an in-memory H2 database. They start the interest job's transaction before the
transfer and use the `Pause` after the debit to make the two cross.
