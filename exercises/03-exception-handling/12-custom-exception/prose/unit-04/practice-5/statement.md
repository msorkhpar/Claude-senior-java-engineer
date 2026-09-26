Try-with-resources closes a reader whatever happens, which keeps code that
deals with checked `IOException`s short; a `BufferedReader` opened that way
suits this task. Write `FirstLine.read(Path path) throws IOException`, which
returns the file's first line.

| file content | `read(path)` |
|---|---|
| `"alpha\nbeta"` | `Optional.of("alpha")` |
| `"only"` | `Optional.of("only")` |

An empty file has no first line, and that is an ordinary outcome, not an
exceptional one. A file that is not there, on the other hand, is a failure the
caller must be told about.
