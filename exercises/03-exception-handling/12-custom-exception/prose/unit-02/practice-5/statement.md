`Function`, `Consumer` and the other standard functional interfaces declare no
checked exceptions, so a lambda body that throws an `IOException` does not
compile. Write `TextLengths.lengths(List<String> names, TextSource source)`
with a stream: map each name to the length of `source.fetch(name)`, in order.

When `fetch` throws an `IOException`, catch it inside the lambda and throw an
`UncheckedIOException` with the message `Could not read <name>`.

| fetched texts | `lengths(List.of("a.txt", "b.txt"), source)` |
|---|---|
| `"abc"`, `"hello"` | `[3, 5]` |
| `"abc"`, then `b.txt` throws `IOException("disk")` | throws `UncheckedIOException("Could not read b.txt")` |

The unchecked wrapper is only a vehicle out of the lambda: whoever catches it
must still be able to get at the original `IOException`.
