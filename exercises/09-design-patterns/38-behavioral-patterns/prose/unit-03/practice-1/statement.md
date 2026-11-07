The team board has an "Archive column" button at the top of every column, and every edit on the
board can be undone. Since archiving shipped, the bug reports say that undoing an archive
sometimes empties the whole board, sometimes brings the columns back without their cards, and
sometimes loses a card that was added just before the archive ran from the board's queue of
pending edits.

`Board`, `History` and the board's own `archiveColumn(...)` and `restore(...)` work. Finish the two
`TODO`s of `ArchiveColumn` in `src/main/java/practice/BoardCommands.java`:

- `execute()`: archive the column;
- `undo()`: put the board back as it was just before `execute()` ran.

The archive is kept sorted, so the order a column's cards had can't be read back from it. When
you're done, undoing one archive, or several in turn, gives back every column with its cards in
their order, and the archive as it was. About eight lines in total.
