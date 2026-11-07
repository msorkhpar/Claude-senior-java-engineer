A warehouse system imports supplier files and reports, for each file, how many rows failed
validation. One `ImportAudit` serves the whole system, and several files can be imported at the
same time on different threads. Rows arrive only through a callback: `RowSource.forEachRow`
hands them one at a time to the action you pass it.

Write `rejected(source)` in `src/main/java/practice/ImportAudit.java`: the number of the file's
rows that `check` doesn't accept.

When you're done, each file's report holds that file's count and nothing else, however many
files were audited before it or alongside it. About four lines.
