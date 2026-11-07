Every night the utility imports its smart-meter readings, about 2.4 million rows, into a database
on another host. `importOneByOne` in `src/main/java/practice/MeterImport.java` does the job
correctly, but the import now runs past the start of the morning shift, and the DBA reports that
it sends a different SQL text for every reading. Meter names come from the vendor's file
unchanged, and some contain quotes.

Write `importAll(connection, readings, chunkSize)` so the import reaches the driver once per
`chunkSize` readings rather than once per reading, sends the database one SQL text for the whole
run, and returns the number of rows the database reports inserted. The connection is the
caller's: don't close it or commit. About twelve lines.

| readings | `chunkSize` | rows stored | calls that send rows to the database |
|---|---|---|---|
| 12 | 4 | 12 | 3 |
| 0 | 50 | 0 | 0 |
| 7 | 3 | 7 | 3 |

The table is `meter_reading(meter VARCHAR(40), wh BIGINT)`.
