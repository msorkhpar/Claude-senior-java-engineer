This builds on the previous practice: `HealthCheck` now keeps its time budget, and its `await` is
the one you wrote.

Probes report a problem by throwing: the Redis probe throws when the connection is refused, and
the replica probe uses `assert`-style checks that throw `AssertionError` when replication lags.
During this morning's Redis outage `/health` answered "postgres PASSED, redis PASSED" for twenty
minutes, while the service's stderr filled up with stack traces from threads named `probe-redis`.

`run(...)` already reports `FAILED` for any probe whose failure is in the `failures` map once its
thread has finished. Finish `probeThread(...)` in `src/main/java/practice/HealthCheck.java` so
that it is.

When you're done, a probe that throws anything at all is reported as `FAILED`. About eight lines.
