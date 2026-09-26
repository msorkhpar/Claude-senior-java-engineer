Intermediate operations are **lazy**: nothing runs until a terminal operation asks
for elements, and a **short-circuiting** terminal operation such as `findFirst()`
stops asking as soon as it has its answer.

Write `FirstMatch.firstMatch(List<String> names, Predicate<String> test, List<String> examined)`.
It returns the first name that `test` accepts, as an `Optional<String>`. Every name
your pipeline actually hands to `test` must also be appended to `examined`, in the
order it was examined, after whatever the list already holds, so the caller can see how far the stream went.

| names | test | answer | examined |
|---|---|---|---|
| `["Alice", "Bob", "Charlie", "David", "Eve"]` | starts with `"C"` | `Optional[Charlie]` | `["Alice", "Bob", "Charlie"]` |
| `["Bob", "Eve"]` | starts with `"Z"` | `Optional.empty` | `["Bob", "Eve"]` |

A pipeline that gathers every match before choosing one gives the right name but
examines too much; and "no match" is an answer too.
