Every night `UsageExport` writes one line per tenant for the billing system, such as
`initech;quota=2500;jobs=1234`. Two tickets are open against it. Last night the job stopped
halfway with an exception, at the first contract tenant whose contract has no quota. And billing
rejects lines where a count of jobs has a decimal point in it.

`line(...)` already builds the line. Finish the two `TODO` methods in
`src/main/java/practice/UsageExport.java`:

- `quota(tenant)`: `100` for a tenant on the free plan, otherwise the quota in its contract, or
  `null` when the contract sets none;
- `value(metric)`: an `Integer` for a count, a `Double` for a ratio.

Both are small choices between two values. About eight lines in total.
