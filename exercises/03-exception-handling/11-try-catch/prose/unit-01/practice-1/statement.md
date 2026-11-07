Every night a job imports the suppliers' price lists into the catalog. Its report counts the rows
it turned away, and the buyers call suppliers about them. Two things went wrong last month. A
supplier renamed a column in its file, the report said every one of its 3,000 rows was rejected,
and the buyers spent a morning on the phone over a file that had no typo in it. And a deploy that
interrupted the job did not stop it: it counted the rest of the file as rejected and finished.

Finish the `TODO` in `importRow(...)` in `src/main/java/practice/PriceImport.java`. A row whose
price is not a whole number of cents is turned away and counted, as before. A file the job can't
read at all fails the import, and an interrupt ends it with the `InterruptedException` that
`importAll` declares, so the job runner sees it. About six lines in total.

The tests hand the job a throttle that never waits but can be interrupted on a chosen call.
