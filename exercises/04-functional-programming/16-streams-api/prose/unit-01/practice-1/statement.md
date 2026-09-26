A stream pipeline has three parts: a **source**, zero or more **intermediate
operations**, and exactly one **terminal operation** that runs the whole thing and
produces a result.

Write `LongNames.upperLongNames(List<String> names)`. It returns the names that are
**longer than three characters**, upper-cased, in the order they came in. Build it as
one pipeline: the list as the source, then the filtering and the upper-casing, then
a terminal operation that collects the result.

| names | answer |
|---|---|
| `["Hi", "Alice", "Jo", "Charlie", "Ed"]` | `["ALICE", "CHARLIE"]` |
| `["Hi", "Jo"]` | `[]` |

Watch the boundary of "longer than three", and remember whose list you were handed:
the caller still owns it after your method returns.
