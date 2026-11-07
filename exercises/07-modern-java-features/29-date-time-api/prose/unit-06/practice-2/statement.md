A hotel's booking form takes the arrival date as text, the way guests in Germany and the Netherlands
write it: day, month and year separated by dots. Some guests pad with zeros and some don't:

| Typed | Arrival |
|---|---|
| `05.01.2032` | 5 January 2032 |
| `5.1.2032` | 5 January 2032 |
| `30.4.2031` | 30 April 2031 |

Last spring the front desk found guests booked for nights they never asked for. A guest who mistyped
the date should get "please check the arrival date" from the form, never a booking for some other
day.

Finish `arrival(...)` in `src/main/java/practice/ArrivalDates.java`: the date the guest typed, or
an empty `Optional` for text that names no date the calendar has. About six lines in total.
