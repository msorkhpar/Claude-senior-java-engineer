This practice continues the `Wallets` of the previous one: `publish()` and `balances()` are done,
and `transfer(...)` is now yours to write.

Players send each other credits in bursts, and the owner thread is often still busy with one
transfer when the next ones are requested. Finance found two wallets below zero last month: each
had been the source of two transfers requested within a few milliseconds, either of which it could
cover alone.

Write `transfer(from, to, credits)` in `src/main/java/practice/Wallets.java`. It returns at once
with a `Future` that says whether the transfer went through. A wallet never goes below zero, and a
transfer requested while an earlier one is still being applied sees that earlier one's result:
credits it brings in can be spent, credits it takes out can't. About eight lines in total.

The tests pause a transfer half-way through `Seam.midTransfer()`, so call it between taking the
credits and adding them.
