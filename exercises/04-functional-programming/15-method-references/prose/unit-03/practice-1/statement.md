The access report is a CSV file built from rows whose cells come from several services, so
`CsvExport` receives each row as a `List<?>`. The auth service keeps session tokens as `char[]`,
so it can wipe them after use. Support has noticed that the token column of last night's report
reads `[C@5e91993f`, `[C@1c4af82c` and so on, and that rows from the batch import show `null`
where a cell was simply empty.

Finish `cell(Object value)` in `src/main/java/practice/CsvExport.java`. A token cell is written as
the characters it holds, an empty cell (`null`) as nothing at all, and every other cell as
`String.valueOf` writes it. `line(...)` already joins the cells with commas. About three lines in total.
