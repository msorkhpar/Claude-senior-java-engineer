The page keeps a running maximum without locks with a `LongAccumulator` built from
`Long::max` and the identity `Long.MIN_VALUE`, and counts under heavy contention with
a `LongAdder`, which spreads contending threads over several cells.

Write `LatencyStats`:

- `record(latency)` counts one value and folds it into the maximum;
- `count()` returns how many values were recorded;
- `max()` returns the largest value recorded, or `Long.MIN_VALUE` when none was;
- `reset()` starts over.

Many threads call `record` at once, so both statistics must be atomic.

| calls | `count()` | `max()` |
|---|---|---|
| none | `0` | `Long.MIN_VALUE` |
| `record(12)`, `record(40)`, `record(7)` | `3` | `40` |
| `record(-5)`, `record(-2)` on a fresh one | `2` | `-2` |
| 4 threads × 1,000 `record`s of `0`…`999` | `4000` | `999` |
| `reset()` | `0` | `Long.MIN_VALUE` |
