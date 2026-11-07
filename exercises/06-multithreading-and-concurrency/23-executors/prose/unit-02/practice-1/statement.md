The finance team reads the nightly settlement report every morning. It has one line per merchant,
and each merchant's day is settled with its bank on a pool, all at once. The line for every
merchant whose bank call went wrong reads `failed: ExecutionException`, so nobody can tell a bank
that timed out from a bug in the ledger. And on the night the ledger database ran out of
connections, the report job died after the first failing merchant, and nobody got a report.

`run(...)` already submits the settlements and builds the report. Finish the `TODO` in
`src/main/java/practice/SettlementReport.java`:

- `line(...)`: wait for one merchant's settlement and write its line.

When you're done, a failed merchant's line names what the bank call threw and its message, every
merchant gets its line whatever happened to the others, and stopping the report job while it waits
ends the job instead of filling the report with failures. About six lines in total.

The tests settle merchants on a real `ExecutorService`.
