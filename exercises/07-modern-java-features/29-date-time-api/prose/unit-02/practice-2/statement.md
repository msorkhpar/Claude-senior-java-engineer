`java.time` has one class per kind of value, and each prints and parses
ISO-8601 by default. A value's ISO text shows which kind it is:

| text | kind |
|---|---|
| `2024-03-15` | a date: `LocalDate` |
| `14:30` | a time of day: `LocalTime` |
| `2024-03-15T14:30` | a date and time with no zone: `LocalDateTime` |
| `2024-03-15T14:30:00Z` | a point on the UTC timeline: `Instant` |
| `2024-03-15T14:30:00+05:30` | a date-time with a UTC offset: `OffsetDateTime` |
| `2024-03-15T14:30:00+05:30[Asia/Kolkata]` | a date-time in a region zone: `ZonedDateTime` |
| `PT2H30M` | a time-based amount: `Duration` |
| `P1Y2M3D` | a date-based amount: `Period` |

Write `IsoText.read(String text)`. It returns the value that `text` spells,
as an object of the class in the table (declared return type `Object`).

- A trailing `Z` means UTC: that is an `Instant`.
- An offset such as `+05:30` with no zone ID is an `OffsetDateTime`; the same
  text followed by a zone ID in brackets is a `ZonedDateTime`.
- An amount starts with `P`. One with a time part (a `T`) is a `Duration`;
  one without is a `Period`.

| call | answer |
|---|---|
| `read("2024-03-15")` | `LocalDate.of(2024, 3, 15)` |
| `read("2024-03-15T14:30:00Z")` | `Instant.parse("2024-03-15T14:30:00Z")` |
| `read("PT2H30M")` | `Duration.ofMinutes(150)` |
