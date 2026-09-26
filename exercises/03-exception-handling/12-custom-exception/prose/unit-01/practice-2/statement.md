`Exception` offers four public constructors, and a custom exception usually
mirrors them. Write the checked `ReportGenerationException` with all four, each
handing its arguments to the matching superclass constructor:

- `ReportGenerationException()`
- `ReportGenerationException(String message)`
- `ReportGenerationException(String message, Throwable cause)`
- `ReportGenerationException(Throwable cause)`

| construction | `getMessage()` | `getCause()` |
|---|---|---|
| `new ReportGenerationException("Report failed", io)` | `"Report failed"` | `io` |
| `new ReportGenerationException(io)` where `io` is `new IOException("disk full")` | `"java.io.IOException: disk full"` | `io` |

Each form must behave exactly as the same form of `Exception` does, including
what it leaves unset. `initCause` is a good way to find out whether a cause was
set at all.
