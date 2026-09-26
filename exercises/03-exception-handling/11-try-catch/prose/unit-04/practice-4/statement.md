When the body of a try-with-resources throws and closing the resource throws too, the body's
exception is the one thrown; the close exception is added to it as *suppressed*, and
`getSuppressed()` returns it. Overlooking suppressed exceptions is one of the page's pitfalls.

Write `LogWriter.write(Opener opener, String text)`. It opens the channel named `"log"`, writes `text`
to it, and closes it. It returns:

- `"ok"` when all of that succeeds;
- otherwise `"failed: " + message` of the `IOException` that was thrown, followed by
  `"; suppressed: " + message` for each exception it suppressed, in order.

| write | close | answer |
|---|---|---|
| succeeds | succeeds | `"ok"` |
| throws `IOException("write failed")` | succeeds | `"failed: write failed"` |
| throws `IOException("write failed")` | throws `IOException("close failed")` | `"failed: write failed; suppressed: close failed"` |

A failure while closing counts as a failure too.
