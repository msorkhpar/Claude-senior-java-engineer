Several catch blocks can follow one `try`, and a multi-catch (`catch (A | B e)`) lets two unrelated
types share one handler instead of duplicating it.

Write `InputHandler.handle(Backend backend, String input)`. It returns one line:

| situation | returns |
|---|---|
| `input` is `null` | `"Invalid input: Input is null"` |
| `input` is empty | `"Invalid input: Input is empty"` |
| `backend.process(input)` returns `r` | `"Input processed successfully: " + r` |
| it throws an `IOException` or a `SQLException` | `"Database or I/O error: " + message` |
| it throws any other exception | `"Unexpected error: " + message` |

Invalid input never reaches the backend. For instance, with a backend that throws
`new SQLException("Simulated SQLException")`, `handle(backend, "x")` returns
`"Database or I/O error: Simulated SQLException"`.

Every row of the table is checked, the unlikely ones included.
