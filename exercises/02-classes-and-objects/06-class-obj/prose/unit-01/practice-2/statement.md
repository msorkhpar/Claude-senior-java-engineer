The sales dashboard keeps the report rows a user looked at in the user's HTTP session, for up to
an hour. Each row came from a `Report` that also holds the rendered PDF, often several
megabytes. Since rows went into the session, the servlet container logs
`NotSerializableException: practice.Report` whenever it persists sessions, and the heap after a
full collection grows with the number of active users.

Finish `row(...)` in `src/main/java/practice/Report.java`, and change `Report.Row` as you need
to. When you're done, a row still shows its sku and its price in its report's currency, it can
be serialized and read back on its own, and it keeps nothing of the report it came from. `Report`
itself stays out of sessions. About six lines in total.
