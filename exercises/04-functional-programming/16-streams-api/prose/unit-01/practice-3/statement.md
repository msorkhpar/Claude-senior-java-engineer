Every night a supplier sends a price file, one `SKU;cents` line per product, with `#` comments
and blank lines in between. Last week a file arrived with a broken line in the middle. The import
should have stopped there so that someone could fix the file and run it again, but half a file
of prices went live, and nobody was told which line was wrong.

`PriceImport` already parses a line (`parse` throws the checked `BadLineException`), recognises
comments and stores a price. Finish the `TODO` in `src/main/java/practice/PriceImport.java`:
`importAll(lines)`, which returns how many prices it stored.

When you're done, a good file is stored in full, and the first bad line ends the import: the
prices before it stay, nothing after it is stored, and the caller's
`catch (PriceImport.BadLineException e)` gets the line's number as an editor would show it. About
eight lines.
