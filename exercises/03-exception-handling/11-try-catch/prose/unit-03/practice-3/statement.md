The nightly reconciliation export holds an export lock while it queries the ledger and writes the
file. Three problems reached the on-call engineer this month:

- an alert read `quota exceeded on /exports`, and the ledger's timeout that actually ended the
  run only turned up the next day, in the database's own log;
- after one such night, no export started again until the service was restarted;
- one file was missing its last rows, and the run had reported success.

`run(...)` already passes `release(...)` whatever ended the export. Finish `release(...)` in
`src/main/java/practice/ReconciliationExport.java`, which closes the writer and releases the
lock, so that none of the three can happen again: the caller of `run(...)` gets the failure that
ended the export, with any failure of the close kept on it, a failure of the close alone is not
hidden, and the lock is free afterwards whatever failed. About eight lines.
