The on-call alert reads the application log and pages someone for every error line. Last night
the payment gateway timed out forty times and nobody was paged. The same morning the log
archiver reported that the share for host `web-07` doesn't exist, although the file server team
can see it.

`LogAlerts` already filters the log; what it filters with, and where it archives to, are the two
`TODO`s in `src/main/java/practice/LogAlerts.java`:

- `errorWord()`: the pattern that finds `ERROR` as a word of its own, so `ERRORS` or `NOERROR`
  don't count;
- `shareFor(host)`: the UNC path of a host's nightly logs, which for `web-07` is
  `\\web-07\logs\nightly` exactly as the file server team wrote it.

When you're done, a line such as `03:10 ERROR timeout` raises the alert, and the archiver gets a
path the file server recognises. About two lines in total.
