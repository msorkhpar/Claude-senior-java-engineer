Every request to the back office appends an event to `AuditLog`: who did what, numbered in order.
By the end of a day the log holds tens of thousands of events. Compliance can start an export at
any moment; it walks the log and writes each event to the archive, which takes a few
milliseconds per event, while requests keep arriving.

Ops wants three things from the log. Request latency must not creep up as the day's log grows.
A request must never wait for an export that happens to be running. An export must finish, with
every event that was in the log when it began, once each and in order; whether it also includes
events appended while it runs doesn't matter.

Finish `src/main/java/practice/AuditLog.java`: choose the collection in `newStore()`, and write
`store` and `export`. About six lines in total.
