A lender's support desk gets the same complaint every spring. A customer whose loan contract says
"due on the 30th" is asked to pay on the 28th in March, April and every month after. Another,
whose first instalment was due on 30 June and whose contract says "the last day of each month",
is asked for 30 July instead of 31 July. Late fees are charged against both schedules.

The contract, as the lawyers wrote it: instalments are due on the same day of the month as the
first one; in a month that has no such day, on that month's last day. A loan whose first
instalment fell on the last day of its month is due on the last day of every month.

`Instalments.schedule` already lists the due dates. Finish the two `TODO`s in
`src/main/java/practice/Instalments.java`:

- `dueDate(first, n)`: the due date of instalment `n`, where instalment `0` is due on `first`;
- `isMonthEnd(date)`: whether a date is the last day of its month.

About four lines in total. The tests use loans that start in 2027 and 2028, leap years included.
