The settlement job nets the day's trades on whatever `ForkJoinPool` the deployment hands it: two
workers in staging, twelve in production. The big clients' trades sit together at the end of the
day's file and take far longer to net than the rest, and in production the job runs about as
long as it would on one thread, while most workers sit idle near the end.

The operations team has set the rule for how the day is cut up: each worker of the pool the job
runs in gets between 32 and 64 pieces of a large day, and a batch of `minRows` trades or
fewer is netted whole, because a piece that small isn't worth a task.

`SettlementJob` already halves the day until a range fits in one piece. Finish `leafSize(rows)` in
`src/main/java/practice/SettlementJob.java`, which says how many trades one piece may hold. About
two lines in total. The tests run the job on real pools of one to forty workers.
