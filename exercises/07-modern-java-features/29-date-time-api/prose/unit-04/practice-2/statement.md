An `Instant` is a point on the UTC timeline; a `LocalDateTime` is a reading of
a wall clock, with no zone. Converting one into the other always needs a
`ZoneId`: the same instant is a different wall-clock reading in each zone, and
the same wall-clock reading is a different instant in each zone.

Write `WallClock`:

- `static LocalDateTime localAt(Instant instant, ZoneId zone)` returns the
  date and time a wall clock in `zone` shows at `instant`.
- `static Instant instantOf(LocalDateTime local, ZoneId zone)` returns the
  instant at which a wall clock in `zone` shows `local`.

| call | answer |
|---|---|
| `localAt(2024-03-15T18:30:00Z, America/New_York)` | `2024-03-15T14:30` |
| `localAt(2024-03-15T18:30:00Z, Asia/Tokyo)` | `2024-03-16T03:30` |
| `instantOf(2024-03-15T14:30, America/New_York)` | `2024-03-15T18:30:00Z` |
| `instantOf(2024-03-15T14:30, Asia/Tokyo)` | `2024-03-15T05:30:00Z` |
