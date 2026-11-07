The nightly reconciliation splits the ledger into shards and matches each shard on a thread of its
own. Finance wants the report at 06:00 whatever happens, so the job has a budget: when it runs
out, the report goes out with whatever has finished. Twice this month the report's total matched
no rerun of the same night, and once the job sat waiting on one stuck shard until an operator
killed it.

`ShardReport.run(...)` already starts the shards; each one adds its matched amounts to its own
slot of a plain `long[]`. Finish `collect(...)` in `src/main/java/practice/ShardReport.java`.

When you're done, the report is ready by the deadline even if a shard is stuck, it totals the
subtotals of the shards that finished, and it lists by index every shard that hadn't, counting
nothing from them. Keep the subtotals in the plain array. About seven lines in total.

The tests hold some shards partway through their work until the test is over.
