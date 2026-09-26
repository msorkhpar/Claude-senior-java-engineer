Java 7's *precise rethrow*: when a `catch (Exception e)` block rethrows `e`
unchanged, the compiler knows which checked exceptions the `try` block can
really throw, so the method declares only those. Write
`Audit.runLogged(Step step, List<String> log) throws IOException`:

- run `step`; if it returns, append `"ok"` to `log`;
- if it throws **any** exception, append `"failed: " + message` and rethrow
  that exception.

| step | log afterwards | outcome |
|---|---|---|
| returns | `["ok"]` | returns |
| throws `IOException("disk full")` | `["failed: disk full"]` | throws that `IOException` |
| throws `IllegalStateException("bug")` | `["failed: bug"]` | throws that `IllegalStateException` |

Keep the `throws` clause to `IOException`. A caller reading the trace of what
you rethrow should see where the failure really happened.
