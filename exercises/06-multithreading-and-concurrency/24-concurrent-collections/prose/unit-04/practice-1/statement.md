The catalogue import loads one batch of SKUs at a time, and the batch header says how many rows
follow. A profile of an import shows time in `HashMap.resize` and `ConcurrentHashMap.transfer` on
every batch, and a heap dump shows the stock cache's table twice the length of the price index's,
for the same number of SKUs.

`CatalogMaps` builds three maps per batch. Finish its three factory methods in
`src/main/java/practice/CatalogMaps.java`:

- `newPriceIndex(skus)`: filled once by the import thread, then only read;
- `newStockCache(skus)`: shared by the request threads;
- `newRecentTable(maxEntries)`: the table behind `RecentLookups`, a cache of the prices users looked
  up most recently, which drops the one used least recently when it is full.

When you're done, none of the three grows its table while a batch loads or while the cache evicts,
and the stock cache's table is no longer than it needs to be. About three lines in total.

The tests read each map's table length by reflection.
