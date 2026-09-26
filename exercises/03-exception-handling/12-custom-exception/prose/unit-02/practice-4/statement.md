Inside a `catch` block, `throw` can raise a new exception that wraps the caught
one as its cause (exception chaining). `ReportException`, a checked exception,
is given. Write `ReportReader`:

- `ReportReader(ReportSource source)`;
- `String read(String name) throws ReportException` returns
  `source.fetch(name)`. When `fetch` throws an `IOException`, `read` throws
  `ReportException` with the message `Could not read report <name>`.

| source behaviour for `"q3"` | `read("q3")` |
|---|---|
| returns `"revenue up"` | `"revenue up"` |
| throws `IOException("Original IO exception")` | throws `ReportException("Could not read report q3")` |

Someone reading the log later still needs to see why the fetch failed, and a
bug in the source (an unchecked exception) is not a failed read.
