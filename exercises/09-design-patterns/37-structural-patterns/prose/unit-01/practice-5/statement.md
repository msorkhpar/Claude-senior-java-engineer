The page's Q4 lists adapters in the JDK itself: `Collections.enumeration`
adapts a collection to the legacy `Enumeration`, and `Collections.list`
adapts an `Enumeration` back. Write the same bridge between the two iteration
interfaces yourself, in both directions (the page's two-way adapter idea):

- `asIterator(enumeration)` returns an `Iterator<T>`: `hasNext()` answers
  `hasMoreElements()`, `next()` answers `nextElement()`.
- `asEnumeration(iterator)` returns an `Enumeration<T>` the other way round.

Like `Arrays.asList`, each result is **a view that reads the source only when
asked**: nothing is copied up front, and each `next()` takes one element.
**Past the end, both throw `NoSuchElementException`**, as their interfaces
require.

| call | walks |
|---|---|
| `asIterator(Collections.enumeration(List.of("x", "y", "z")))` | `x`, `y`, `z` |
| `asEnumeration(List.of(1000, 2000).iterator())` | `1000`, `2000` |
| `next()` after `z` | `NoSuchElementException` |
