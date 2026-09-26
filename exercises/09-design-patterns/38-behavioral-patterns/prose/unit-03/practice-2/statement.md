The page's invoker keeps **two stacks**: executed commands on the undo
stack, undone commands on the redo stack. Its critical rule: **a new
command clears the redo stack**, or redo would replay a history that no
longer exists. It also advises a size limit, because an unbounded undo stack
keeps growing.

`Command` (given) has `execute()`, `undo()` and `description()`. Write
`CommandHistory`:

- `new CommandHistory(limit)` keeps at most `limit` commands to undo
  (`limit < 1` throws `IllegalArgumentException`). When a new command would
  go past the limit, **the oldest one is dropped**.
- `executeCommand(c)` runs `c`, pushes it on the undo stack and clears the
  redo stack (`null` throws `IllegalArgumentException`).
- `undo()` pops the newest command, undoes it and pushes it on the redo
  stack; `redo()` pops from the redo stack, executes it again and pushes it
  back on the undo stack. **With nothing to undo (or redo), they return
  `false`**; otherwise `true`.
- `canUndo()`, `canRedo()`, `undoSize()`, `redoSize()`, and
  `getUndoHistory()`: the descriptions on the undo stack, **newest first**.

| calls (each command adds its number to a total) | total | `getUndoHistory()` |
|---|---|---|
| execute `add 1`, execute `add 20` | 21 | `["add 20", "add 1"]` |
| then `undo()` | 1 | `["add 1"]` |
| then `redo()` | 21 | `["add 20", "add 1"]` |
| undo, then execute `add 300`, then `redo()` → `false` | 301 | `["add 300", "add 1"]` |
