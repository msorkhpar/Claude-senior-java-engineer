A legacy `java.util.Date` reports its year as years since 1900
(`getYear()` is `124` for 2024), and both `Date.getMonth()` and
`Calendar.MONTH` count months from 0 (January is `0`, December is `11`).
A lenient `Calendar` also accepts impossible values and silently rolls them
over: month index `12` becomes January of the next year, and February 30
becomes early March.

You receive such legacy fields from an old system. Write `LegacyFields`:

- `static LocalDate fromLegacy(int yearsSince1900, int zeroBasedMonth, int dayOfMonth)`
  returns the calendar date the fields name.
- `static int[] toLegacy(LocalDate date)` returns the three legacy fields
  `{yearsSince1900, zeroBasedMonth, dayOfMonth}` for `date`.

| call | answer |
|---|---|
| `fromLegacy(124, 2, 15)` | `2024-03-15` |
| `fromLegacy(99, 11, 31)` | `1999-12-31` |
| `toLegacy(LocalDate.of(2024, 3, 15))` | `{124, 2, 15}` |

Fields that name no real date are an error, not a date to guess at:
`fromLegacy` **throws** `java.time.DateTimeException` for them and never rolls
them over the way a lenient `Calendar` does.
