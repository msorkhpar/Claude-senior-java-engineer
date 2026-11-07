The warehouse's `Timesheets` class serves two teams. Payroll pays workers from it, and floor
planning builds its weekly cover report from it. Both have always started from the same number,
`worked(shift)`: time on site less the break.

Two requests arrived for this month's release:

- **Payroll**, after the new union agreement: each shift is paid in whole quarter hours of time
  worked. A shift with 8h20 worked is paid for 8h15.
- **Floor planning**: a break shorter than 20 minutes is taken at the workstation, so the report
  counts it as time on the floor. A longer break doesn't count.

Finish the two `TODO`s in `src/main/java/practice/Timesheets.java`, `Payroll.paidMinutes(...)` and
`FloorReport.floorMinutes(...)`, and change anything else in the file you need to. When you're
done, each team gets its new rule, and neither team's numbers change because of the other team's
request. About four lines in total.
