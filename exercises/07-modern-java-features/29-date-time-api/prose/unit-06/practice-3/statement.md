An events feed gives each event's local time as text, but not always in full:
sometimes with seconds, sometimes without, and for all-day events as a date
alone.

Write `EventTimes.parse(String text)`, returning a `LocalDateTime`:

- `2024-03-15T10:30:45` is that date and time;
- `2024-03-15T10:30` has **no seconds**: they are zero;
- `2024-03-15` has **no time at all**: it means the start of that day, `00:00`.

**Any other text throws** `DateTimeParseException`: a fraction of a second, seconds
without a time, or a date or time that does not exist.

| text | answer |
|---|---|
| `2024-03-15T10:30:45` | `2024-03-15T10:30:45` |
| `2024-03-15T10:30` | `2024-03-15T10:30` |
| `2024-03-15` | `2024-03-15T00:00` |
| `2024-03-15 10:30` | `DateTimeParseException` |
| `2024-03-15T10:30:45.5` | `DateTimeParseException` |
| `2024-03-15:45` | `DateTimeParseException` |
| `2024-02-30` | `DateTimeParseException` |
