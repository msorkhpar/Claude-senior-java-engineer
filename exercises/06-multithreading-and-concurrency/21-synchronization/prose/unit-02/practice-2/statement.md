The page compares three tools. `volatile` gives visibility, and atomic reads and
writes of one variable, **including** `long` and `double`, but it never makes
`count++` atomic. `AtomicInteger`/`AtomicLong` (or `LongAdder`) make a compound
update atomic without a lock. `synchronized` makes a whole block atomic.

Write `ServerStatus`, choosing the right tool for each of its three fields:

- **Up or down**, written by one thread and read by many: `markUp()`,
  `markDown()`, `isUp()`. A new status is down.
- **Requests served**, incremented by many threads at once: `request()`,
  `requests()`. It must never lose an increment.
- **Last restart**, a `long` of epoch milliseconds written by one thread:
  `restartedAt(long millis)`, `lastRestart()`. A reader must never see half of
  one write and half of another. It starts at `0`.

The tests look at the field each method writes and check its kind.

| calls | answer |
|---|---|
| `new ServerStatus().isUp()` | `false` |
| `markUp()`, `isUp()` | `true` |
| three `request()`, `requests()` | `3` |
| `restartedAt(1_700_000_000_123L)`, `lastRestart()` | `1700000000123` |
