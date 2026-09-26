Never leave a catch block empty: at the very least, record what went wrong.

Write `BatchImporter.sum(List<String> rows, List<String> log)`. It parses every row with `Integer.parseInt`, exactly
as written (no trimming; a row too large for an `int` is bad), and
returns the sum of the rows that parse. A row that does not parse is skipped, and one line is added
to `log` for it: `"row <n>: <message>"`, where `<n>` counts rows from 1 and `<message>` is the
message of the exception `Integer.parseInt` threw.

| rows | returns | log afterwards |
|---|---|---|
| `["1", "2", "3"]` | `6` | `[]` |
| `["1", "x", "3"]` | `4` | `["row 2: For input string: \"x\""]` |

Every kind of bad row goes to the log, including one that is not there at all (`null`), whose line also
carries the message `Integer.parseInt` threw for it.
