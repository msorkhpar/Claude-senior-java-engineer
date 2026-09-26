Case labels stacked on one body share it: execution falls through the empty labels into
the statements below them. The `default` case catches every value no label names.

Write `days(int month, int year)` in `Calendar`, with a `switch` statement. It returns the
number of days in `month` (1 to 12) of `year`:

- January, March, May, July, August, October and December have `31`;
- April, June, September and November have `30`;
- February has `29` in a leap year and `28` otherwise. A year is a leap year when it is
  divisible by 4, except that a year divisible by 100 is not, unless it is also divisible
  by 400: `2024` and `2000` are leap years, `2023` and `1900` are not;
- any other month number returns `-1`.

Examples: `days(1, 2023)` is `31`, `days(4, 2023)` is `30`, `days(2, 2024)` is `29`,
`days(2, 1900)` is `28`, and `days(13, 2023)` is `-1`.
