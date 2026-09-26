A handler should be able to react to *which* file failed without parsing the
message. Write the checked `FileProcessingException` and a `FileProcessor` that
uses it.

1. `FileProcessingException(String message, String fileName, Throwable cause)`
   and `getFileName()`.
2. `FileProcessor(FileStore store)` and `int countLines(String fileName)`: read
   the file through `store.read(fileName)` and return how many lines it has
   (`String.lines()` counts them). When the store throws an `IOException`,
   throw `FileProcessingException` with the message
   `Error processing file: <fileName>` and that file name.

| store behaviour for the name | `countLines` |
|---|---|
| `"notes.txt"` reads `"a\nb\nc"` | `3` |
| `"empty.txt"` reads `""` | `0` |
| `"blank-line.txt"` reads `"\n"` | `1` |
| `"nonexistent.txt"` throws `IOException("File not found")` | throws `FileProcessingException("Error processing file: nonexistent.txt")`, `getFileName()` is `"nonexistent.txt"` |

The tests' store fails only with `IOException`; what happens to any other
exception from the store is not part of this task.

The exception may be caught, logged and rethrown by several layers: think about
what the original failure should look like to the last of them, and whether any
of them should be able to change the exception's data.
