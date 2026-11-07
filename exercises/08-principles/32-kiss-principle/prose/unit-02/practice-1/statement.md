A game keeps its players' credits in `Wallets`. One owner thread applies every transfer, in the
order they are requested. The leaderboard and the support console read balances from their own
threads, many times a second, and support agents have complained twice: once about a balance
sheet that added up to less than the credits in circulation, and once about a console that froze
for as long as a large transfer took.

Finish the two `TODO`s in `src/main/java/practice/Wallets.java`:

- `publish()`, which the owner thread already calls once the opening balances are in place and
  after every transfer that changed them;
- `balances()`, which any thread may call.

When you're done, a reader always gets balances as they stand between two transfers, never in the
middle of one, and gets them without waiting for a transfer in progress. Balances a reader already
holds don't change afterwards, and nothing one reader does to its copy reaches another reader.
About two lines in total.

The tests pause a transfer half-way through `Seam.midTransfer()` and read from another thread
while it is paused.
