Exception chaining wraps a caught exception inside another one before rethrowing it: the new
exception adds context, and the original rides along as its **cause**, stack trace and all.

Write `ConfigLoader.load(Source source, String name)`. It reads the source once and returns `source.read(name)`. When the read
throws an `IOException`, `load` throws a `ConfigException` (declared in the starter) whose message
is `"Error processing file " + name`.

| source | `load(source, "app.conf")` |
|---|---|
| reads `"port=80"` | returns `"port=80"` |
| throws `IOException("disk gone")` | throws `ConfigException("Error processing file app.conf")` |

Whoever debugs the `ConfigException` later must still be able to reach the `IOException` that caused
it. And wrap what the page calls a file-processing failure, nothing broader: any exception other than an
`IOException` reaches the caller unchanged.
