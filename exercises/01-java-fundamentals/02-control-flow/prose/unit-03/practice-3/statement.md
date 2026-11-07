The nightly reconciliation checks every invoice, shard by shard. Invoice ids are positive `int`s,
and each shard owns an inclusive range of them. The last shard's range ends at the largest `int`,
and when shards are rebalanced some of them are left with an empty range, whose first id is above
its last.

Every night, every shard reports done except the last one. Its worker is still at 100 % CPU in
the morning, and the reconciliation table has rows for invoice ids below zero.

Write `forEachId(first, last, visit)` in `src/main/java/practice/InvoiceSweep.java`: hand every
id of the range to `visit`, once each and in increasing order, for every shard. About five lines.
