The finance team pastes the monthly CSV exports into spreadsheets that read columns by position.
`CsvExport` turns a list of records into CSV, and for a sealed family of records, such as every
kind of delivery, it writes one `## TypeName` section per record type, empty ones included, so
every month's file has the same shape.

The writing is done. Finish the three `TODO` methods in `src/main/java/practice/CsvExport.java`:

- `columns(...)`: the column names of a record type;
- `valueOf(...)`: one record's value in one column;
- `recordTypes(...)`: every record type of a sealed family, which may contain sealed interfaces of
  its own.

When you're done, a record exports exactly its own values, under columns in the order the record
declares them, on any JVM, and a family export has one section per record type of the family and
no others. About ten lines in total.
