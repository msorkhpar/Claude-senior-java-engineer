Several catch blocks can follow one `try`, and a multi-catch (`catch (A | B e)`) lets two unrelated
types share one handler instead of duplicating it.

Write `InputHandler.handle(Backend backend, String input)`. It returns one line:

| situation | returns |
|---|---|
| `input` is `null` | `"Invalid input: Input is null"` |
| `input` is empty | `"Invalid input: Input is empty"` |
| `backend.process(input)` returns `r` | `"Input processed successfully: " + r` |
| it throws an `IOException` or a `SQLException` | `"Database or I/O error: " + message` |
| it throws any other `Exception` | `"Unexpected error: " + message` |

Here `message` is the thrown exception's own `getMessage()`. "Empty" means `isEmpty()`: blank input
such as `"  "` is valid and goes to the backend exactly as given. Invalid input never reaches the
backend, and an `Error` is not reported: it reaches the caller. For instance, with a backend that throws
`new SQLException("Simulated SQLException")`, `handle(backend, "x")` returns
`"Database or I/O error: Simulated SQLException"`.

Every row of the table is checked, the unlikely ones included.
