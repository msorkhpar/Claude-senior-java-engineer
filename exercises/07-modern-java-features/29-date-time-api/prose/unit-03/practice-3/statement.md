`java.time` has no business-day support of its own, but `LocalDate.datesUntil`
streams every date of a range, and `DayOfWeek` says which ones are weekends.

Write `BusinessDays`:

- `static long count(LocalDate start, LocalDate end, Collection<LocalDate> holidays)`
  returns how many dates from `start` (included) to `end` (excluded) are
  Monday to Friday and are not in `holidays`.
- `static LocalDateTime add(LocalDateTime from, int days)` returns the
  date-time `days` business days after `from`, at the same time of day.
  Saturdays and Sundays are skipped (holidays play no part here).

| call | answer |
|---|---|
| `count(2024-03-11, 2024-03-18, [])` (Monday to Monday) | `5` |
| `count(2024-03-16, 2024-03-18, [])` (a weekend) | `0` |
| `count(2024-03-11, 2024-03-18, [2024-03-13])` | `4` |
| `count(2024-03-11, 2024-03-18, [2024-03-16])` (a Saturday) | `5` |
| `add(2024-03-15T17:45, 1)` (a Friday) | `2024-03-18T17:45` |
| `add(2024-03-14T08:00, 3)` (a Thursday) | `2024-03-19T08:00` |
| `add(2024-03-16T10:00, 5)` (a Saturday) | `2024-03-22T10:00` |

A holiday is a date: two `LocalDate` objects for the same day are the same
holiday, however they were made.
