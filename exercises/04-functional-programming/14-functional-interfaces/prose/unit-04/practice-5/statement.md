The marketing dashboard shows, for each user, the page they landed on: their first visit in the
time range. One `VisitReport` object serves the dashboard while the application runs, and the
dashboard asks it for a new report every few minutes, with that period's visits in the order they
happened.

Write `firstPer(...)` and `landingVisits(...)` in `src/main/java/practice/VisitReport.java`.
`firstPer(key)` is a reusable building block: a predicate that passes the first element it sees
for each key and rejects the ones after it. `landingVisits` uses it to list each user's first
visit, in the order the visits happened. About four lines in total.
