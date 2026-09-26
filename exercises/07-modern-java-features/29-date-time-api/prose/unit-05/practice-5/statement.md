A storage service keeps each file for a retention amount read from its
configuration, written in ISO-8601: `P6M`, `P1Y2M3D`, `PT90M`, `PT2H30M`.

Write `Retention.expiresAt(ZonedDateTime created, String amount)`, the moment
the file expires:

- An amount with a time part (after a `T`, as in `PT24H` or `P1DT12H`) is exact
  elapsed time: `PT24H` is 24 hours and `P1DT12H` is 36 hours.
- An amount with **only date parts** (`P...` with no `T`) is a calendar amount,
  counted on the calendar in `created`'s zone. That holds for `P1D` too: one
  day later is the same wall-clock time the next day.
- Text that is not such an amount is **refused with `IllegalArgumentException`**.

So `P1M` is a month and `PT1M` is a minute.

| created (America/New_York) | amount | answer |
|---|---|---|
| `2024-01-15T09:00` | `P6M` | `2024-07-15T09:00` |
| `2024-01-15T09:00` | `PT90M` | `2024-01-15T10:30` |
| `2024-01-15T09:00` | `P1M` | `2024-02-15T09:00` |
| `2024-01-15T09:00` | `PT1M` | `2024-01-15T09:01` |
| `2024-03-09T12:00` | `P1D` | `2024-03-10T12:00` (the clocks sprang forward in between) |
| `2024-03-09T12:00` | `PT24H` | `2024-03-10T13:00` |
| `2024-01-15T09:00` | `6 months` | `IllegalArgumentException` |
