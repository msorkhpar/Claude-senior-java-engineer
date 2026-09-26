`Instant.truncatedTo(ChronoUnit.SECONDS)` drops everything below the second,
which normalises instants of different precision before they are compared or
grouped: `100.5 s` and `100.999 s` both truncate to second `100`.

Write `EventRate.perSecond(List<Instant> events)`. It returns, in time order,
how many events happened in each whole second: the key is the second the event
happened in (the event's instant truncated to seconds), the value is the count.
Seconds with no event do not appear.

| events (seconds since the epoch) | answer |
|---|---|
| `100.1`, `100.4`, `101.2` | `{100 s: 2, 101 s: 1}` |
| `100.6`, `100.999`, `101.001` | `{100 s: 2, 101 s: 1}` |
| `-0.5`, `0.2` | `{-1 s: 1, 0 s: 1}` |

Each key is an `Instant` on a whole second, such as `Instant.ofEpochSecond(100)`.
