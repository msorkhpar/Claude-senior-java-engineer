A care home pays its staff for the hours they work beyond their contract, and deducts the hours
they fall short. Payslips show the week's balance as hours and minutes: `2:30` over, `-0:40`
short. Since the night team joined, payroll has seen a full week of nights shown as dozens of hours
short, a balance printed as `0:-40`, and a nurse who worked a week of double shifts shown as only
a few hours over.

`Timesheet.balance(...)` already adds up the shifts and subtracts the contract. Finish the two
`TODO`s in `src/main/java/practice/Timesheet.java`:

- `length(start, end)`: how long one shift lasts, given the times on the clock;
- `label(amount)`: the text payroll reads, hours then a colon and two-digit minutes, with a
  minus sign in front when the amount is negative.

About four lines in total.
