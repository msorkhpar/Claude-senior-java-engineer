A wrapped exception keeps the exception it wraps as its cause, and `getCause()` gives it back, so a
chain of wrapped exceptions can be walked link by link.

Write `Causes.find(Throwable thrown, Class<T> type)`. It returns the first throwable in the chain
`thrown`, `thrown.getCause()`, `thrown.getCause().getCause()`, … that is a `type`, or an empty
`Optional` when none is.

Given `io = new IOException("disk")` and
`outer = new IllegalStateException("load failed", new RuntimeException("wrapped", io))`:

| call | answer |
|---|---|
| `find(outer, IOException.class)` | `Optional.of(io)` |
| `find(outer, RuntimeException.class)` | `Optional.of(outer)` |
| `find(outer, SQLException.class)` | `Optional.empty()` |

The chain may be of any length, and the nearest match to `thrown` wins. Mind where the chain starts, and what "is a `type`" means for subclasses.
