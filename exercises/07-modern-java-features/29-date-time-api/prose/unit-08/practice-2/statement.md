A `java.util.Date` is a moment: milliseconds since 1970-01-01T00:00Z, with no
calendar day and no wall clock of its own. Turning it into a `LocalDate`, or a
`LocalDateTime` into a `Date`, therefore needs a zone.

Write two methods on `LegacyDays`:

- `dayOf(Date date, ZoneId zone)`: the calendar day `date` falls on in `zone`.
- `toDate(LocalDateTime local, ZoneId zone)`: the `Date` for the moment that
  `local` names on `zone`'s clock.

Use **the zone passed in**, and only that one: the server's default zone must
make no difference, and it is never changed.

| call | answer |
|---|---|
| `dayOf(2024-03-15T23:30:00Z, UTC)` | `2024-03-15` |
| `dayOf(2024-03-15T23:30:00Z, Asia/Tokyo)` | `2024-03-16` |
| `dayOf(2024-03-15T23:30:00Z, America/New_York)` | `2024-03-15` |
| `dayOf(2024-01-15T04:30:00Z, America/New_York)` | `2024-01-14` |
| `dayOf(2024-07-15T04:30:00Z, America/New_York)` | `2024-07-15` |
| `toDate(2024-03-15T10:00, UTC)` | the `Date` of `2024-03-15T10:00:00Z` |
| `toDate(2024-03-15T10:00, America/New_York)` | the `Date` of `2024-03-15T14:00:00Z` |

(A `Date` is shown here by the instant it holds.)
