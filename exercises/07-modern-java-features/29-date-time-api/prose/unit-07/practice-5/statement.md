A status page shows the current time in several offices. `WorldClock` is built
with the `Clock` it must read (tests pass a fixed or a moving clock; nothing
reads the system clock directly).

Write `WorldClock.show(List<ZoneId> zones)`. It returns one line per zone, in the
order given: the zone ID, a space, then the time in that zone as the short
English weekday, the 24-hour time and the zone's short name.

All the lines describe **one moment**. A clock keeps moving while the lines are
built, so the lines must all come from **a single reading** of the clock, taken **when
`show` is called**: a later call shows a later moment. The weekday and zone name
are **English whatever the server's default locale** is.

For the moment `2024-03-15T12:30:00Z`:

| zone | line |
|---|---|
| `America/New_York` | `America/New_York Fri 08:30 EDT` |
| `Europe/London` | `Europe/London Fri 12:30 GMT` |
| `Asia/Tokyo` | `Asia/Tokyo Fri 21:30 JST` |
