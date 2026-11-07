Compliance archives every tenant's audit log once a month. The audit service hands a log out in
pages: `fetch(null)` returns the first page, and each page carries the cursor that fetches the
page after it, or `null` on the last one. A page can come back empty while the service moves a
tenant between partitions, and still carry a cursor.

Last month's archive files were empty for every tenant, and nobody noticed for three weeks.

Write `exportAll()` in `src/main/java/practice/AuditExport.java`: every event of the log, in the
order the service returns them, whatever the number of pages. About six lines.
