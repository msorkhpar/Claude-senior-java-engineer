The legacy `TimeZone.getTimeZone(id)` never complains: an ID it does not
recognise silently gives GMT, and it still accepts three-letter abbreviations
such as `"CST"`, which could mean Central Standard Time or China Standard Time.
A typo in a configuration file therefore turns into wrong times, not an error.

Write `ZoneLookup.zone(String id)`. It returns the `ZoneId` for a region ID
such as `"Europe/Paris"`. An ID that names no zone is refused with an
`IllegalArgumentException`, and so are the ambiguous abbreviations `"CST"`
(Central or China Standard Time) and `"IST"` (India, Israel or Irish Standard
Time).

| id | answer |
|---|---|
| `"Europe/Paris"` | `ZoneId.of("Europe/Paris")` |
| `"Asia/Tokyo"` | `ZoneId.of("Asia/Tokyo")` |
| `"Mars/Olympus_Mons"` | throws `IllegalArgumentException` |
| `"CST"`, `"IST"` | throws `IllegalArgumentException` |
