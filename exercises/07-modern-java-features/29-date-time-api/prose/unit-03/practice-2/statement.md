A contractor bills from a generated timesheet, one row per day. Clients on a monthly contract
get a row for each day of the month; clients who order a fixed engagement get a row for each
weekday of it, and their purchase order prints the engagement as a first and a last day ("from
2027-10-01 to 2027-10-07"). Last month a client paid for one day less than the contractor worked,
and nobody could say which.

`Timesheet` already knows which days are weekdays. Finish the two `TODO`s in
`src/main/java/practice/Timesheet.java`:

- `days(month)`: every day of a calendar month, in order;
- `weekdays(first, last)`: the weekdays of an engagement, from its first day to its last, in order.

About three lines in total. The tests use months and engagements in 2027 and 2028.
