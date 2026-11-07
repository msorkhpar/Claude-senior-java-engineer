Each warehouse has a legacy table with a `TIMESTAMP` column (no zone) for the time a parcel was
scanned. The warehouse staff read that column directly, so it must show what the clocks in the
warehouse showed at the scan. The old DAO takes and returns `java.sql.Timestamp`, and writes the
date and time a `Timestamp` gives through `toLocalDateTime()`. The new scanning service, written
with `java.time`, runs on servers whose default zone nobody controls, and it serves warehouses in
several zones.

Finish the two `TODO` methods in `src/main/java/practice/ScanTimeColumn.java`. Each
`ScanTimeColumn` is built with its warehouse's zone:

- `toColumn(scanned)`: the value to hand the DAO for a scan at that moment;
- `fromColumn(column)`: the moment of the scan that a row read by the DAO records.

The tests use warehouses in two zones and never change the JVM's default zone. About two lines in
total.
