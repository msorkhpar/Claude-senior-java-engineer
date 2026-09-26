Any class that implements `AutoCloseable` can be declared in a try-with-resources statement, and its
`close()` is called automatically when the block exits.

Write the class `Session implements AutoCloseable`:

- `Session(String name, List<String> log)` opens a session that records what happens in `log`;
- `void use(String what)` adds `"use " + what` to the log;
- `void close()` adds `"close " + name` to the log (it throws no checked exception);
- `boolean isClosed()` says whether **this** session has been closed (each session keeps its own state,
  even when two share a name and a log).

```
try (Session s = new Session("db", log)) {
    s.use("query");
}
// log is now ["use query", "close db"]
```

Page's pitfall: a resource is closed after its try block, so using it there must fail with
`IllegalStateException("Resource is already closed")`. And a resource may be closed more than once
by careless callers.
