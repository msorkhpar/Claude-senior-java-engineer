The checkout service's dashboard has a "busy workers" gauge, and the autoscaler adds instances when
it stays above 80% of capacity. The gauge used to count the live threads whose names started with
`checkout-`. Since checkout moved to virtual threads, the gauge reads 0 at the busiest hour of the
day, and the autoscaler has stopped adding instances.

`WorkerGauge` is the `ThreadFactory` the service now uses, with
`Executors.newThreadPerTaskExecutor(gauge)`. Finish the two `TODO`s in
`src/main/java/practice/WorkerGauge.java`: `newThread(...)` makes the thread for one request, and
`busy()` reports how many of this gauge's requests are in progress. About eight lines in total.

When you're done, `busy()` follows the requests that are running: up while they run or wait,
down when they end, however they end.
