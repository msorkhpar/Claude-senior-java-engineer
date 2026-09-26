Opening hours are `LocalTime` values: a time of day with no date and no zone.
A `LocalTime` only runs from `00:00` (`LocalTime.MIDNIGHT`) to
`23:59:59.999999999` (`LocalTime.MAX`), so a window that closes after midnight
has a closing time that is *earlier* than its opening time.

Write `OpeningHours.isOpen(LocalTime time, LocalTime open, LocalTime close)`.
It returns whether `time` is within the window from `open` to `close`, both
ends included. When `close` is before `open`, the window runs overnight, from
`open` through midnight to `close`. When `open` equals `close`, the window is
that single moment, not the whole day.

| time | open | close | answer |
|---|---|---|---|
| `12:00` | `09:00` | `17:00` | `true` |
| `17:01` | `09:00` | `17:00` | `false` |
| `17:00` | `09:00` | `17:00` | `true` |
| `17:00:30` | `09:00` | `17:00` | `false` |
| `12:00` | `09:00` | `09:00` | `false` |
| `02:00` | `22:00` | `06:00` | `true` |
| `12:00` | `22:00` | `06:00` | `false` |
