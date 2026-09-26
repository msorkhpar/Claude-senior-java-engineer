Code inside a `catch` block can throw too, and when it does, the exception that was being handled
is lost unless you keep it.

Write `FallbackReader.read(Source primary, Source backup, String name)`:

- it returns `primary.read(name)`;
- when that throws an `IOException`, it returns `backup.read(name)` instead;
- when the backup throws an `IOException` as well, `read` throws **the backup's** exception, with the
  primary's exception added to it through `addSuppressed`.

| primary | backup | result |
|---|---|---|
| reads `"a"` | (not called) | `"a"` |
| throws `IOException("primary down")` | reads `"b"` | `"b"` |
| throws `IOException("primary down")` | throws `IOException("backup down")` | throws "backup down", suppressing "primary down" |

A backup is for I/O failures only; a bug in the primary is not something to paper over.
