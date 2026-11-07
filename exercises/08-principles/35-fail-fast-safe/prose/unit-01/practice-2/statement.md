The nightly report export reads a small settings file when it starts. Last month an operator set
`export.batchsize=40` to ease the load on the database; the export ran with batches of 500 all
week, and nobody knew until the database team asked. Another night the file was deployed without
its `export.dir` line, and the reports were written to whatever directory the exporter happened to
start in.

`ExportSettings.from(...)` already builds the settings from the three methods it calls. Finish the
three `TODO`s in `src/main/java/practice/ExportSettings.java`, using `DEFAULTS` and `REQUIRED`.

When you're done, a file the exporter can use starts it, with the defaults filling in the optional
settings the file leaves out. A file it can't use stops it with an `IllegalArgumentException` whose
message names the setting at fault. About twelve lines in total.
