A birthday is a `LocalDate`: only the date matters. Here "today" comes from
a `Clock` passed in rather than from `LocalDate.now()`, so a test can fix the
moment and the zone.

Write `Ages`:

- `static int ageOn(LocalDate birth, LocalDate on)` returns the number of whole
  years from `birth` to `on`. A year counts only once the birthday has been
  reached. A `birth` after `on` is refused with an `IllegalArgumentException`.
- `static int ageToday(LocalDate birth, Clock clock)` is the age on today's
  date, where today is the date `clock` shows **in the clock's own zone**.

| call | answer |
|---|---|
| `ageOn(1990-06-15, 2024-06-15)` | `34` |
| `ageOn(1990-06-15, 2024-06-14)` | `33` |
| `ageOn(2000-03-01, 2023-03-01)` | `23` |
| `ageToday(1990-06-15, Clock.fixed(2024-06-14T20:00:00Z, Asia/Tokyo))` | `34` (it is already June 15 in Tokyo) |
