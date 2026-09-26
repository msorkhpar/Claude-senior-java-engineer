The page's isolation table says which anomalies each level allows. You can
watch them with two connections driven step by step in one thread: the
reader reads, the writer changes the row, the reader reads again.

Write `Isolation.readTwice(reader, level, accountId, between)` over
`accounts(id INT PRIMARY KEY, balance DECIMAL(15,2))`. It returns
`Reads(first, second)`, the account's balance read twice with
`between.run()` called in the middle (the tests' writer acts there).

- **Both reads run in one transaction**: turn autoCommit off before the first
  read and commit after the second.
- **The transaction runs at the isolation level asked for**
  (`Connection.TRANSACTION_...`).
- **The connection's previous isolation level and autoCommit are restored**
  afterwards; a pool will hand it to someone else.

| level | writer, between the reads | `Reads` |
|---|---|---|
| READ_COMMITTED | sets 200.00 and commits | `100.00, 200.00` (non-repeatable read) |
| REPEATABLE_READ | sets 200.00 and commits | `100.00, 100.00` |
| READ_UNCOMMITTED | sets 200.00, not committed | `100.00, 200.00` (dirty read) |
