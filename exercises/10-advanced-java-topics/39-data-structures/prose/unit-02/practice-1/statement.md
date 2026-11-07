At month end the warehouse reconciles what it shipped against what finance billed. The step
that finds the unbilled lines took a few seconds when the business was small; now each month is
several times larger than the last, and the step's run time grows much faster than the month.

`Reconciler` already prints the report line. Finish `unbilled(...)` in
`src/main/java/practice/Reconciler.java`: it returns the shipped items that are not among the
billed ones, in shipping order, with repeats, and changes neither input. Items are compared with
`equals()` and `hashCode()`; they are not `Comparable`.

When you're done, doubling both inputs roughly doubles the work this method does. About five
lines in total.

The tests use items that count the `equals()` and `hashCode()` calls made on them.
