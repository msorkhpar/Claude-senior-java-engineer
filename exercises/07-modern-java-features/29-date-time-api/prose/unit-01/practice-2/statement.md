`SimpleDateFormat.parse` is lenient by default: it reads `2024-13-01` as a
date in the next year and `2024-02-30` as a day in March, without a word.
Even when made strict, it stops reading once it has a date and ignores any
text left over. `java.time` validates strictly and fails fast instead.

Write `DateInput.parse(String text)`. `text` must be **exactly** a date in the
form `yyyy-MM-dd` (a four-digit year, a two-digit month, a two-digit day).
Return it as a `LocalDate`. For any other text, including a date that does not
exist, throw an `IllegalArgumentException`.

| text | answer |
|---|---|
| `"2024-03-15"` | `2024-03-15` |
| `"2024-02-29"` | `2024-02-29` (2024 is a leap year) |
| `"2023-02-29"` | throws `IllegalArgumentException` |
| `"2024-13-01"` | throws `IllegalArgumentException` |
| `"2024-03-15 extra"` | throws `IllegalArgumentException` |
| `"+12024-03-15"`, `"2024-3-5"`, `" 2024-03-15"` | throws `IllegalArgumentException` |
