The catalog service applies the supplier's price feed every night and republishes the catalog
when any price changed. The feed is not trusted: some lines name no SKU, and some carry a price
of zero or less. Both kinds must be skipped, and neither may stop the sync.

`CatalogSync` already reads the feed, applies a single change and reports. Finish the two `TODO`
methods in `src/main/java/practice/CatalogSync.java`:

- `acceptable(change)`: whether a line of the feed may be applied, as its comment defines it;
- `step(changedSoFar, change)`: one step of the sync, called once per line, in order.

When you're done, every acceptable line reaches the catalog, no other line touches it, and
`sync` returns `true` exactly when some price changed. About two lines in total.
