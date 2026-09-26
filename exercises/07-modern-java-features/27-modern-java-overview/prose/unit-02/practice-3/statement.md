Java 11 added `Files.writeString()` and `Files.readString()`, which the page
says simplify small-file I/O dramatically: no readers, writers or loops.
`writeString` takes `OpenOption`s such as `StandardOpenOption.CREATE` and
`StandardOpenOption.APPEND`; without options it creates the file or
**replaces** its content.

Write the class `Notes`, which keeps one note per line in a small text file:

- `append(Path file, String note)` adds `note` and a `"\n"` at the **end** of
  the file, creating the file when it does not exist. Notes already in the file
  stay.
- `read(Path file)` returns the notes in order. A file that does **not exist**
  holds no notes: return an empty list.

| calls, on a file that does not exist yet | answer |
|---|---|
| `read(file)` | `[]` |
| `append(file, "buy milk")`, then `read(file)` | `["buy milk"]` |
| then `append(file, "call Bob")`, then `read(file)` | `["buy milk", "call Bob"]` |
