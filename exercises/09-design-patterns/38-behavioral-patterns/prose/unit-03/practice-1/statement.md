A command turns a request into an object: it holds its **receiver** and its
parameters, and knows how to `execute()` and `undo()` it. The page's first
pitfall is a delete that cannot be undone because nothing remembered the
deleted text. **The text must be saved when `execute()` runs**, not when the
command is built: the page's key point 7.

`Command` and the receiver `TextEditor` are given (`insert(pos, text)`,
`delete(pos, len)`, `getContent()`). Write:

- `InsertCommand(editor, position, text)`: `execute()` inserts `text` at
  `position`; `undo()` removes that text **from that same position**.
  `description()` is `"Insert '<text>' at position <position>"`.
- `DeleteCommand(editor, position, length)`: `execute()` saves the text it
  is about to delete, then deletes it; `undo()` puts that text back.
  **`undo()` before any `execute()` throws `IllegalStateException`** and
  changes nothing. `description()` is
  `"Delete <length> chars at position <position>"`.
- Both constructors refuse a `null` editor (and `InsertCommand` a `null`
  text) with `IllegalArgumentException`.

| text before | command | after `execute()` | after `undo()` |
|---|---|---|---|
| `"Hello"` | `Insert(5, " World")` | `"Hello World"` | `"Hello"` |
| `"xyz"` | `Insert(1, "ab")` | `"xabyz"` | `"xyz"` |
| `"Hello World"` | `Delete(5, 6)` | `"Hello"` | `"Hello World"` |
| `"abc"` | `Delete(0, 1)`, never executed | | `IllegalStateException` |
