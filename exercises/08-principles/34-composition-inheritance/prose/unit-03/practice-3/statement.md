This practice starts from the previous one: `MeteredStock` is written, and it now also has a
busy gauge, `inFlight()`, the number of calls that have started and not yet ended.

The checkout threads are moving onto the warehouse's `MapStock`, which was written for one
thread. Nobody wants to change `MapStock` or the checkout code. Finish, in
`src/main/java/practice/Stocks.java`:

- `SynchronizedStock.level` and `SynchronizedStock.set`, a wrapper that makes any `Stock` safe
  to share between threads;
- `shared(base)`, which wraps `base` for the checkout threads and returns the stock they call
  together with the `MeteredStock` the dashboard reads.

When you're done, the checkout threads can call the shared stock at the same time without
corrupting it, a refused update (a negative count) doesn't stop other threads from using it,
and the dashboard's busy gauge shows how many calls are working in the store right now, not
how many are waiting their turn. About eight lines in total.
