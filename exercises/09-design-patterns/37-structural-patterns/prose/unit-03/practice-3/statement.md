The treasury team imports the bank's payment file every night, one CSV row per payment. A row
without an amount must be rejected and leave nothing behind, while the rows before and after it
are imported. Last week a bad row left a "payment from" entry with no amount in the ledger, and
reconciliation took a day.

`importRow` is already marked `@Transactional`, and `PaymentImport.transactional(...)` is the
proxy that runs such methods in a transaction. Finish the two `TODO`s in
`src/main/java/practice/PaymentImport.java`:

- `CsvRows.importAll(...)`: import the rows, rejecting the bad ones;
- `create(...)`: build the importer the application calls.

When you're done, each good row commits on its own, a bad row rolls back only itself and shows
up in `rejected()`, and the ledger holds only complete payments. You may add a field. About six
lines in total.
