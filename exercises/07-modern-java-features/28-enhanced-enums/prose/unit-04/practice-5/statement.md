The page's `CompareStrategy` is a fixed set of singleton comparison strategies
whose method is generic: `<T> int compare(T a, T b)`. Each strategy converts
into a standard `Comparator<T>`, so it composes with the Comparator API.

Fill in each constant's `compare`, and write the three shared methods:

- `NATURAL`: natural order. If either value is not `Comparable`, throw
  `UnsupportedOperationException("Not comparable")`.
- `REVERSE`: the reverse of natural order, with the same check.
- `BY_STRING`: compare `String.valueOf(a)` with `String.valueOf(b)` exactly
  (case counts, and `null` reads as `"null"`).
- `<T> Comparator<T> toComparator()`: this strategy as a `Comparator<T>`.
- `<T> T min(List<T> items)` and `<T> T max(List<T> items)`: the least and the
  greatest item by this strategy; an empty list throws
  `NoSuchElementException`.

| call | answer |
|---|---|
| `NATURAL.min(List.of("Charlie", "Alice", "Bob"))` | `"Alice"` |
| `REVERSE.min(List.of("Charlie", "Alice", "Bob"))` | `"Charlie"` |
| `BY_STRING.min(List.of(9, 10, 100))` | `10` |
| `NATURAL.compare(new Object(), new Object())` | throws `UnsupportedOperationException` |
| `NATURAL.max(List.of())` | throws `NoSuchElementException` |
