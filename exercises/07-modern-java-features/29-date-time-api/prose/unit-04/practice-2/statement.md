A team wiki keeps its pages in a table whose `updated_at` column holds epoch milliseconds. To stop
two people from overwriting each other, every edit carries the `updated_at` of the version it was
made from, and the store refuses the edit if that is no longer the current version.

`PageStore` creates, reads and saves pages, and it reads time from the `Clock` it is given. Finish
its two `TODO` methods in `src/main/java/practice/PageStore.java`:

- `stamp()`: the moment recorded as a new version's `updated_at` and returned to the client;
- `isCurrent(...)`: whether a client's stamp names the version the column holds.

When you're done, a client can edit a page with the stamp it got from `create`, `read` or `save`,
and an edit made from a replaced version is refused, however soon after the replacement it
arrives. About two lines in total.

The tests drive the store with a clock they move by hand, and its readings carry nanoseconds.
