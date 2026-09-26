An old library hands you `java.util.Calendar` objects and wants them back.
A `Calendar` is mutable, and the library keeps using the ones it gave you.

Write two methods on `LegacyCalendars`:

- `toZoned(Calendar calendar)`: the same moment as a `ZonedDateTime` in the
  calendar's **own time zone**. Most calendars are `GregorianCalendar`s, but
  **any** `Calendar` must convert, such as the Japanese imperial calendar.
- `plusOneDay(Calendar calendar)`: a new `Calendar`, one calendar day later at
  the **same wall-clock time** in the same zone. The caller's calendar is
  **never changed**.

| calendar (zone, moment) | toZoned | plusOneDay |
|---|---|---|
| UTC, `2024-03-15T10:00:00Z` | `2024-03-15T10:00` at that moment | `2024-03-16T10:00:00Z` |
| `America/New_York`, `2024-03-15T14:00:00Z` | `2024-03-15T10:00-04:00[America/New_York]` | |
| Japanese, `Asia/Tokyo`, `2024-03-15T10:00:00Z` | `2024-03-15T19:00+09:00[Asia/Tokyo]` | |
| `America/New_York`, `2024-03-09T17:00:00Z` (noon) | | `2024-03-10T16:00:00Z` (noon again) |
