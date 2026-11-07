The security report shows a timeline of logins. Each login is recorded twice, by the gateway and
by the session store, and both copies reach `LoginAudit` as `java.util.Date` values of the same
millisecond. On some days the timeline shows every login once; on others, the same logins show up
twice, and nobody changed the code in between.

Write `distinctMoments` in `src/main/java/practice/LoginAudit.java`: every moment at which a login
happened, earliest first, each once, whichever source delivered it first. About five lines in
total.
