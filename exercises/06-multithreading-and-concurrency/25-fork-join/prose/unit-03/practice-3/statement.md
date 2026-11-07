The bank's ledger files arrive at night: a few million rows, one amount per row, and the import
totals them on a `ForkJoinPool`. When a file has a bad row the whole file is rejected and someone
fixes it by hand in the morning. The scheduler creates one `LedgerImport` at startup and uses it
for every file of the night. Two complaints from the people who fix the files:

- the rejection in the log reads
  `java.lang.IllegalArgumentException: java.lang.IllegalArgumentException: row 88412: not an amount: 1.204,00`,
  and the ticketing system, which expects `row 88412: not an amount: 1.204,00`, files it under
  "unknown";
- after a rejection, the import keeps all the CPUs busy for as long as a good file takes.

Finish `original` and `compute()` of the `Total` task in
`src/main/java/practice/LedgerImport.java`. When you're done:

- a good file is totalled, and a file with a bad row ends `total` with an
  `IllegalArgumentException` whose message is exactly `"row " + index + ": "` and the parser's
  message;
- once a row has turned out bad, the rest of the file is left mostly unparsed;
- a rejected file doesn't change what the next file on the same `LedgerImport` returns.

About fifteen lines in total. The tests run real pools of two and three workers.
