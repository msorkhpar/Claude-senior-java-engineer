The warehouse keeps its stock in one `ConcurrentHashMap`: for each SKU, how many units sit in
each bin. Pickers move units between bins all day. The same map is read by the hourly stock
count and updated by the order service, which ships units straight from it with its own
`compute` calls. Lately the stock count has reported units missing that turned up an hour later,
a bin once showed -6 units, and a shipment of glue reappeared on the shelf in the system but not
in the aisle.

Finish `move(...)` in `src/main/java/practice/Warehouse.java`. The `Bins` record and the reading
methods are done. The order service won't change, so whatever `move` does must hold against its
calls on the map. When you're done, a stock count taken at any moment finds every unit, no bin
goes below zero, and no shipment is undone. About seven lines in total.

The tests hold the picker's thread right after it reads the map, or right after it first changes
it, and meanwhile take a stock count or ship from the same bin.
