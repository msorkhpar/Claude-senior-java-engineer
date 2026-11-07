The catalogue import reads exports from several suppliers. Each supplier has its own delimiter
character and a fixed number of columns, and any column may be left empty. The import has been
fine with the suppliers that use commas and semicolons. A new supplier's file was rejected line
by line, and lines from the old ones whose last column is empty end up on the review list.

Finish the `TODO` in `src/main/java/practice/DelimitedFile.java`: `fields(line)` returns the
line's fields when it has exactly one per column, and nothing otherwise.

When you're done, every supplier's well-formed lines are read, whatever the delimiter, and only
lines with the wrong number of fields are rejected. About three lines in total.
