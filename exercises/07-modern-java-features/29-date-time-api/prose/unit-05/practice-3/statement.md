A timesheet lists shifts, each a start and an end `LocalTime`. Write
`Timesheet.total(List<Shift> shifts)`: the total time worked, as the hours, a
colon and the minutes in two digits (`7:45`, `0:05`, `41:30`).

- A shift whose end is before its start **runs past midnight**: `22:00` to
  `06:00` is 8 hours. An end of `00:00` is midnight at the end of the shift.
- The total can exceed a day, and **every hour is shown**: 28 hours and 30
  minutes is `28:30`.

`Shift(LocalTime start, LocalTime end)` is given. Shift times are whole
minutes, no shift is longer than 24 hours, and no shift has its end equal to its start.

| shifts | answer |
|---|---|
| `09:00-12:30`, `13:00-17:15` | `7:45` |
| (none) | `0:00` |
| `22:00-06:00` | `8:00` |
| `20:00-00:00` | `4:00` |
| three of `06:00-13:00` and one `06:00-13:30` | `28:30` |
