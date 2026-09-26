A stream pipeline has three parts: a source, zero or more intermediate
operations, and exactly one terminal operation that runs the whole thing and
produces a result.

Write `LongNames.upperLongNames(List<String> names)`. It returns the names that are
**longer than three characters** (every character counts, spaces included),
upper-cased with `Locale.ROOT`, in the order they came in; a name that repeats is
kept each time. One pipeline
fits it well: the list as the source, the filtering and the upper-casing, then a
terminal operation that collects the result.

| names | answer |
|---|---|
| `["Hi", "Alice", "Jo", "Charlie", "Ed"]` | `["ALICE", "CHARLIE"]` |
| `["Hi", "Jo"]` | `[]` |
| `["Alice", " Jo ", "Alice"]` | `["ALICE", " JO ", "ALICE"]` |

Watch the boundary of "longer than three", and remember whose list you were handed:
the caller still owns it after your method returns.
