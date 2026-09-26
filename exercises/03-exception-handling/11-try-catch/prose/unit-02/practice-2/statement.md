Catch blocks are evaluated in order, top to bottom, and the first one whose type matches wins. So
the more specific types go first and the most general last.

Write `TaskRunner.run(Callable<String> task)`. It calls the task and returns one line:

| the task | returns |
|---|---|
| returns `r` | `"done: " + r` |
| throws a `FileNotFoundException` | `"missing: " + message` |
| throws any other `IOException` | `"io: " + message` |
| throws any other `Exception` | `"failed: " + message` |

| task | answer |
|---|---|
| `() -> "42"` | `"done: 42"` |
| throws `new FileNotFoundException("a.txt")` | `"missing: a.txt"` |
| throws `new IOException("disk")` | `"io: disk"` |
| throws `new IllegalStateException("nope")` | `"failed: nope"` |

A type is matched with its subclasses (an `EOFException` is an `IOException`). Report exceptions, and
only exceptions: anything thrown that is not an `Exception` reaches the caller unchanged.
