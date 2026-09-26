An `enum` makes a switch type-safe: its cases name the constants directly
(`case MONDAY:`), and no other value can reach it.

A shop opens `9` hours on weekdays, `5` hours on Saturday and not at all on Sunday. Write
`hours(DayOfWeek day)` in `Opening`, using `java.time.DayOfWeek` and a `switch` statement
whose cases name the days:

- `hours(DayOfWeek.MONDAY)` to `hours(DayOfWeek.FRIDAY)` are `9`;
- `hours(DayOfWeek.SATURDAY)` is `5`;
- `hours(DayOfWeek.SUNDAY)` is `0`.
