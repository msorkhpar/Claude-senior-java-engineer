A `TreeSet` keeps its elements sorted, which makes **range queries** and
**neighbour queries** cheap: `subSet(from, fromInclusive, to, toInclusive)`,
`ceiling(x)` (the smallest element `>= x`) and `floor(x)` (the largest
`<= x`). The page uses them on a set of scores.

Write `Scoreboard`:

- `new Scoreboard(scores)` keeps each score **once**, as a set does.
- `between(low, high)` lists the scores from `low` to `high`, **both ends
  included**, ascending, as a new list (`low > high` is refused with
  `IllegalArgumentException`).
- `atLeast(x)` is the smallest score `>= x` and `atMost(x)` the largest
  `<= x`; **an exact match counts**, and when there is none the result is
  `OptionalInt.empty()`.

Scores: 50, 70, 85, 90, 95, 100.

| call | result |
|---|---|
| `between(60, 92)` | `[70, 85, 90]` |
| `between(70, 95)` | `[70, 85, 90, 95]` |
| `atLeast(80)` / `atMost(80)` | `85` / `70` |
| `atLeast(85)` | `85` |
| `atLeast(101)` | empty |
