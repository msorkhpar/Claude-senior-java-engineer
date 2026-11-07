The reporting service reads report texts from a file share that is slow, so it keeps each text in
memory after the first read. The cache is meant to be built on `Map.computeIfAbsent`. Its callers
already handle failures by type: a `FileNotFoundException` becomes "no such report" for the user,
any other `IOException` becomes "try again later", and an unchecked exception is a bug that goes
to the error tracker.

Write `get(...)` and `fetchForCache(...)` in `src/main/java/practice/ReportCache.java`. When you're
done, `get` caches each text, and when the source fails, the caller receives exactly the exception
the source threw, checked or unchecked. About eight lines in total.
