A reminder app offers two kinds of "tomorrow". Write three methods on
`Reminders`, for times in any zone:

- `sameTimeTomorrow(ZonedDateTime t)`: the **same wall-clock time** on the next
  calendar day, however long that day is.
- `exactlyADayLater(ZonedDateTime t)`: the moment **exactly 24 hours** after `t`,
  whatever the wall clock then shows.
- `hoursBetween(ZonedDateTime a, ZonedDateTime b)`: the whole hours that really
  **elapse** from `a` to `b` (negative when `b` is before `a`). A part of an
  hour does not count: 1 hour 40 minutes is `1`, and minus 1 hour 30 minutes is
  `-1`.

Both methods that return a time keep `t`'s zone.

In `America/New_York` the clocks jump from 02:00 to 03:00 on 2024-03-10, and from
02:00 back to 01:00 on 2024-11-03. So those two days last 23 and 25 hours.

| t (America/New_York) | sameTimeTomorrow | exactlyADayLater |
|---|---|---|
| `2024-03-15T09:00` | `2024-03-16T09:00` | `2024-03-16T09:00` |
| `2024-03-10T00:00` | `2024-03-11T00:00` | `2024-03-11T01:00` |
| `2024-11-03T00:00` | `2024-11-04T00:00` | `2024-11-03T23:00` |

| a | b | hoursBetween |
|---|---|---|
| `2024-03-15T09:00` | `2024-03-16T09:00` | `24` |
| `2024-03-10T00:00` | `2024-03-11T00:00` | `23` |
| `2024-11-03T00:00` | `2024-11-04T00:00` | `25` |
