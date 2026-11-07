Customers upload CSV files of contacts, and the import runs in the background: each chunk of the
file is parsed, its rows are staged, and the staged rows appear in the customer's account when
the import commits. A support ticket says: "I pressed Cancel because I picked the wrong file. The
screen said the import was cancelled. An hour later, all 40,000 contacts were in my account."

`run(...)` already imports correctly. Finish the three `TODO`s in
`src/main/java/practice/CsvImport.java`:

- `cancel(...)`: what the Cancel button does;
- `parse(...)`: one chunk, which may take a while;
- `mayCommit()`: the last word before the rows become visible.

When you're done, an import the user cancels commits nothing, whether the click arrives while it
waits on the storage or just before it commits. About six lines in total.

The tests run imports on a real `ExecutorService` and cancel them through the `Future` that
`start` returns.
