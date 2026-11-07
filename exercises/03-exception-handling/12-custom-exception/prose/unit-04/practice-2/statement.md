Every night the operations team counts the error lines in each host's log. Most hosts write
UTF-8; the old warehouse hosts write ISO-8859-1, and `countAnyCharset` already reads a log again
in that charset when it isn't valid UTF-8. Logs live on a network mount, so now and then a read
fails, and `scan` already skips such a log and reports why. Last night the whole batch stopped at
the first warehouse log, and the logs after it were never counted.

Finish `count(log, charset)` in `src/main/java/practice/ErrorCounter.java`. It reads the log with
`BufferedReader.lines()` and counts the lines the team's `Rule` calls errors.

When you're done, a warehouse log is counted in its own charset, a log that can't be read is
reported as skipped while the rest of the batch is counted, and a broken `Rule`, which is a bug
for its team to fix, still stops the batch. About six lines in total.

The tests hand the counter logs that live in memory; they decode, and fail, the way
`Files.newBufferedReader` does.
