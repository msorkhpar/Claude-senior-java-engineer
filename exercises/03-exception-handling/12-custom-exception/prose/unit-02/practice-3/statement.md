The stack trace is captured when the exception object is **created**, not
where it is thrown, so a pre-built exception kept in a field points at the
wrong place in the logs. Write `Connection`:

- `String send(String message)` returns `"sent: " + message` while the
  connection is open;
- `void close()` closes it;
- after `close()`, `send` throws `IllegalStateException("Connection closed")`.

| calls | result |
|---|---|
| `send("hi")` | `"sent: hi"` |
| `close(); send("hi")` | throws `IllegalStateException("Connection closed")` |

Anyone reading the trace of a failed `send` should be led straight to `send`,
every time it fails.
