`LocalDate` has no `getQuarter()`, and calendar logic such as "the last day of
the quarter" is best written as a `TemporalAdjuster`, applied with `with(...)`
just like the built-in ones in `TemporalAdjusters`. Quarter 1 is January to
March, quarter 2 April to June, quarter 3 July to September and quarter 4
October to December.

Write `Quarters`:

- `static int quarterOf(LocalDate date)` returns the quarter (1 to 4) of `date`.
- `static TemporalAdjuster endOfQuarter()` returns an adjuster that moves a
  date to the last day of its quarter. It must work on any temporal that has a
  date, such as a `LocalDateTime`, and leave everything else about it (such as
  the time of day) as it was.

| call | answer |
|---|---|
| `quarterOf(2024-08-15)` | `3` |
| `quarterOf(2024-03-31)` | `1` |
| `LocalDate.of(2024, 8, 15).with(endOfQuarter())` | `2024-09-30` |
| `LocalDate.of(2024, 2, 10).with(endOfQuarter())` | `2024-03-31` |
| `LocalDateTime.of(2024, 8, 15, 10, 15).with(endOfQuarter())` | `2024-09-30T10:15` |
