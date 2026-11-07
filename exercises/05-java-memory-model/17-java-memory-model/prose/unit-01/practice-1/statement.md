Checkout prices every basket in euros through `RateTable`, which loads exchange rates from the
rate store on first use and again after each `refresh()`. A full load takes a few seconds.

Two incidents shaped what is wanted. The first version held a lock while it loaded, and after
every refresh the checkout threads queued behind the load: p99 latency jumped by four seconds.
The fix for that answered some baskets with "no rate for NOK" for as long as a load was running.

Finish the two `TODO`s in `src/main/java/practice/RateTable.java`:

- `table()`: return the current table, loading it if there is none;
- `load()`: one full load from the store.

When you're done, a lookup never waits for a load another thread started (two threads loading at
the same time is acceptable, as both get the same rates), a lookup never sees a table that is
missing rates the store has, and the map `rates()` hands to the end-of-day report can't be used to
change the rates every other caller sees. About eight lines in total.

The tests hold the store in the middle of a load and make lookups from other threads meanwhile.
