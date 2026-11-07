The billing job summarises each electricity meter's day: one reading per minute, in watt-hours,
and `MeterDay.GAP` for a minute in which the meter sent nothing. Billing stores the energy used
and the number of missing minutes; the support team reads the longest outage, the longest run of
missing minutes.

Meters on a flaky mobile link miss minutes every day, and the support team wants to see whether
an outage was still going on at midnight.

Finish the `TODO` inside the loop of `summarise(...)` in
`src/main/java/practice/MeterDay.java`. When you're done, the summary is right for any day,
wherever its missing minutes fall, and the job never stalls on one. About eight lines in total.
