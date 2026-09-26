A batch scheduler reads each job's start as a local date and time in a zone.
Twice a year that local time may be special:

- In the spring **gap** the clocks skip an hour: in `America/New_York`,
  2024-03-10 goes from 01:59 straight to 03:00, so 02:30 never happens.
- In the autumn **overlap** they repeat one: on 2024-11-03, 01:00 to 01:59
  happens first at `-04:00`, then again at `-05:00`.

Write `WallClock.at(LocalDateTime local, ZoneId zone)`, the job's start as a
`ZonedDateTime`, under the scheduler's rules:

- a time the clocks skip is **refused** with `IllegalArgumentException`, so the
  operator picks a real time; it is not quietly moved;
- a time the clocks repeat is taken at its **second** occurrence, after the
  clocks went back.

| local | zone | answer |
|---|---|---|
| `2024-03-15T02:30` | `America/New_York` | `2024-03-15T02:30-04:00[America/New_York]` |
| `2024-03-10T02:30` | `America/New_York` | `IllegalArgumentException` |
| `2024-11-03T01:30` | `America/New_York` | `2024-11-03T01:30-05:00[America/New_York]` |
| `2024-03-31T01:30` | `Europe/London` | `IllegalArgumentException` |
| `2024-10-06T02:15` | `Australia/Lord_Howe` (a 30-minute gap) | `IllegalArgumentException` |
| `2024-10-27T01:30` | `Europe/London` | `2024-10-27T01:30Z[Europe/London]` |
