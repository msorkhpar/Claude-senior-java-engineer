The search reindexer walks a range of record IDs in batches. A tenant whose IDs were allocated
from the top of the `int` range, up to `Integer.MAX_VALUE`, has to be reindexed too, and so does a
range whose size isn't a multiple of the batch size.

Finish the `TODO` in `src/main/java/practice/IdBatches.java`: `forEachBatch(...)` hands the range
`firstId` to `lastId`, both included, to the handler in batches of at most `size` IDs, in order.

When you're done, every ID of the range is handed over exactly once, whatever `int` the range ends
at, and the method returns once the last batch is handed over. About four lines in total.
