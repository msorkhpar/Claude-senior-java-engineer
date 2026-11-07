Accounts receivable posts each invoice with `InvoicePosting.post(...)`: a row in the ledger, the
PDF in the archive, and one database transaction around both. Two things have to hold for the
people around it.

The nightly job catches `SQLException` from `post(...)` and retries the invoice when the database
reports a serialization conflict (SQLState `40001`); anything else it reports. And last month an
invoice in a currency nobody had enabled made the ledger code throw, the transaction stayed open,
and the connection pool ran dry an hour later.

`write(...)` already does the work of one posting, commit included. Finish the `TODO` in
`src/main/java/practice/InvoicePosting.java`:

- `post(...)`: begin the transaction and run `write(...)`. Whatever goes wrong, the transaction is
  rolled back and the caller sees the failure exactly as it was thrown. Keep the method's
  signature.

About seven lines in total.

The tests hand the posting a ledger, an archive and a transaction that record every call.
