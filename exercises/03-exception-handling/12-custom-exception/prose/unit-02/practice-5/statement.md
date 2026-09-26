`Function`, `Consumer` and the other standard functional interfaces declare no
checked exceptions, so a lambda body that throws an `IOException` does not
compile. Write `TextLengths.lengths(List<String> names, TextSource source)`:
the length of `source.fetch(name)` for each name, in order.

When `fetch` throws an `IOException`, throw an `UncheckedIOException` with the
message `Could not read <name>`. The exercise is meant for a stream whose
`map` lambda catches the `IOException` and wraps it; the tests grade the
result and the exception, not whether you used a stream.

| fetched texts | `lengths(List.of("a.txt", "b.txt"), source)` |
|---|---|
| `"abc"`, `"hello"` | `[3, 5]` |
| `"abc"`, then `b.txt` throws `IOException("disk")` | throws `UncheckedIOException("Could not read b.txt")` |

The unchecked wrapper is only a vehicle out of the lambda: whoever catches it
must still be able to get at the original `IOException`.
