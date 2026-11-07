The search service rebuilds its index in partitions: `jobs(n)` returns one job per partition,
and the jobs go to a pool that runs them in whatever order its threads pick them up, and runs a
failed job again. After the last release, a 16-partition rebuild reindexed one partition sixteen
times and left the other fifteen stale.

Finish `jobs(...)` in `src/main/java/practice/Reindex.java`. When you're done, job number `i`
reindexes partition `i`: whenever it runs, in whatever order, and as often as the pool runs it.
About five lines in total.
