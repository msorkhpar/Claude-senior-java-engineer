The reporting service lets a user pick the format of a report, and `Exports.forFormat(...)` hands
back the matching exporter. The files are supposed to be named after their format, but support
keeps receiving CSV and JSON reports named `something.dat`, which nothing opens with the right
program.

Finish `fileName(...)` in `src/main/java/practice/Exports.java`. You may change how the exporters
declare their extensions; keep each exporter's extension the same text it is now. When you're
done, every exporter names its files with its own extension, however the code that holds it
declared its type, including an exporter whose format builds on another one. About five lines in
total.
