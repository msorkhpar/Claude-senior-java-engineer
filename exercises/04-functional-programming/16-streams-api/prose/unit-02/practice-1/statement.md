The support team's service runs `LogScanner.countErrors(dir)` every few minutes to show how many
`ERROR` lines today's logs hold. The numbers are right, and the service runs fine for a day or two.
Then every scan, and everything else in the process that opens a file, fails with
`Too many open files`, until someone restarts it.

Finish the two `TODO` methods in `src/main/java/practice/LogScanner.java`:

- `listLogs(dir)`: the `.log` files of the directory, sorted by name;
- `errorsIn(log)`: how many lines of one log contain `ERROR`.

Open the directory with `openDir` and each log with `openLog`, never with `Files` directly: the
tests replace those two methods to watch what the scanner opens. A log can fail while it is being
read (a disk unmounted, a file truncated under you); that failure should reach the caller of
`countErrors`. About seven lines in total.

When you're done, the scanner can run every few minutes for weeks and hold no file open between
scans, whether a scan succeeds or fails.
