Users of the form editor report that Undo is unpredictable: sometimes it takes back an edit they
made minutes ago instead of the one they just made, and pressing it once too often shows an error
page. `UndoHistory` keeps the edits in an `ArrayDeque`, and its `menu(n)` already lists them for
the Undo menu by iterating that deque.

Finish the two `TODO`s in `src/main/java/practice/UndoHistory.java`:

- `record(edit)`: remember the edit; the history holds at most `limit` edits, and when it is full
  the edit made longest ago is forgotten;
- `undo()`: take back the latest edit not yet undone, or return an empty `Optional` when there is
  none.

When you're done, Undo always takes back the most recent remaining edit, the menu lists edits in
the order Undo will take them, an empty history is not an error, and a full history drops its
oldest edit. About three lines in total.
