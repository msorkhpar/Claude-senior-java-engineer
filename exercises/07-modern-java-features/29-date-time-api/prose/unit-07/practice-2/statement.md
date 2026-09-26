Two feeds report the same events, each in its own zone, so one moment can
arrive as `2024-03-15T12:00-04:00[America/New_York]` and again as
`2024-03-15T16:00Z[Europe/London]`.

Write `Events.distinctMoments(List<ZonedDateTime> events)`. It keeps the first
event for each **moment on the timeline**, and drops every later event at that
same moment, whatever zone or offset it is written in. Events at the same
wall-clock time in different zones are different moments. The events it keeps
stay in **the order they came in**.

| events | answer |
|---|---|
| `12:00 New York`, `13:00 New York`, `12:00 New York` | `12:00 New York`, `13:00 New York` |
| `12:00:00 New York`, `12:00:00.5 New York` | both: half a second apart is another moment |
| `12:00 New York`, `16:00 London`, `16:00Z[UTC]`, `12:00-04:00` | `12:00 New York` |
| `12:00 New York`, `12:00 London`, `12:00 Tokyo` | all three |
| `13:00 New York`, `16:00 London`, `12:00 New York` | `13:00 New York`, `16:00 London` |

(All on 2024-03-15.)
