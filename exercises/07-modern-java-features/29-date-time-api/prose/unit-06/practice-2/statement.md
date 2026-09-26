A form lets users type a date in any of three forms:

| form | example |
|---|---|
| year-month-day | `2024-03-15` |
| month/day/year | `03/15/2024` |
| day-month abbreviation-year, in US English | `15-Mar-2024`, `15-Sep-2024` |

Write `UserDates.parse(String text)`. It returns the date in an `Optional`, or
an empty `Optional` when the text is not a date in one of these forms. It never
throws.

- A day the month does not have is **refused**: `2024-02-30` is not a date, and
  must not become February 29.
- A `null` text gives an empty result.
- The month abbreviations are US English (`Jan`, `Feb`, `Mar`, ... `Sep`, ...) **whatever the
  server's default locale** is.

| text | answer |
|---|---|
| `"2024-03-15"` | `Optional[2024-03-15]` |
| `"03/15/2024"` | `Optional[2024-03-15]` |
| `"15-Mar-2024"` | `Optional[2024-03-15]` |
| `"2024/03/15"` | `Optional.empty` |
| `"2024-02-30"` | `Optional.empty` |
| `"02/29/2024"` | `Optional[2024-02-29]` |
| `null` | `Optional.empty` |
