The nightly import reads CSV rows into DTOs through `RowBinder`, which was written when every DTO
was a JavaBean. This sprint the team turned the DTOs into records, and the first import after the
release failed on its first row with `NoSuchMethodException: ...<init>()`.

`RowBinder.bind` already sends records to `bindRecord`. Finish the `TODO` in
`src/main/java/practice/RowBinder.java` so that `bindRecord` builds the record from the row. The
row is a map from column name to value, as the CSV reader produced it: numbers arrive boxed, and
the columns come in the file's order, which is not always the order of the record's components.
When a record refuses a value, the import reports the record's own exception, the one its author
wrote the message for.

About twelve lines in total.
