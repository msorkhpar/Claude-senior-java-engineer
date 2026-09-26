The page's `unsafeCast` returns `(List<String>) obj` under
`@SuppressWarnings("unchecked")`. The cast checks nothing about the elements,
so a list holding `42` gets through and fails later, wherever `get()` meets
the `Integer`. The page's advice is to suppress only a warning you have
justified, and otherwise to fix the underlying issue.

Write `Casts.strings(Object obj)`, which needs no suppression at all:

- `obj` must be a `List`: anything else, **a `Set` included**, is refused
  with `IllegalArgumentException`.
- **Every element is checked at the call**: an element that is not a
  `String` (or is `null`) is refused with `IllegalArgumentException` before
  anything is returned.
- The result holds the same strings in the same order and is **a copy**:
  later changes to the source list do not reach it.

| `obj` | result |
|---|---|
| a list holding `"a"`, `"b"` | `[a, b]` |
| a list holding `"a"`, `42` | `IllegalArgumentException` |
| `"abc"` | `IllegalArgumentException` |
| `Set.of("a")` | `IllegalArgumentException` |
