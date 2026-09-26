A constant can take several constructor parameters, and some of them may be
absent. The page's advice is to expose a nullable parameter as an `Optional`
rather than scatter `null` checks. `WorkSchedule` gives each day a
`DayOfWeek`, a start and an end time as `"HH:mm"` text (both `null` on
Sunday), and whether the day is required.

The constants, fields and constructor are given. Write:

- `Optional<LocalTime> start()`: the start time, or empty when there is none.
- `Duration hours()`: the time from start to end, or `Duration.ZERO` when the
  day has no times.
- `boolean isWorkDay()`: whether the day has working times at all.
- `static WorkSchedule forDate(LocalDate date)`: the constant for the date's
  day of the week.
- `static Duration requiredHoursPerWeek()`: the total `hours()` of the
  **required** days only.

| call | answer |
|---|---|
| `MONDAY.start()` | `Optional[09:00]` |
| `FRIDAY.hours()` | `PT7H` |
| `SATURDAY.isWorkDay()` | `true` (but Saturday is not required) |
| `SUNDAY.hours()` | `PT0S` |
| `forDate(LocalDate.of(2026, 9, 25))` | `FRIDAY` |
| `requiredHoursPerWeek()` | `PT39H` |
