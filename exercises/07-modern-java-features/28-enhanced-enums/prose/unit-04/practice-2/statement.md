The page's `CollectionOp` shows the most common generic enum method shape:

```java
public abstract <T> List<T> execute(List<T> input, Predicate<T> predicate);
```

`T` is inferred from the caller's list, so
`FILTER.execute(List.of("a", "bb", "ccc"), s -> s.length() > 1)` is a
`List<String>` with no cast. The page also warns against returning a raw
`List`: the result must be a properly typed **new** list.

Fill in each constant's `execute`, and the one-argument convenience overload:

- `FILTER`: the elements matching `predicate`, in order.
- `SORT`: the elements in natural order (they are `Comparable`); the predicate
  is ignored.
- `DISTINCT`: each element once, at its first occurrence, in the input's
  order; the predicate is ignored.
- `REVERSE`: the elements in reverse order; the predicate is ignored.
- `<T> List<T> execute(List<T> input)`: `execute` with a predicate that accepts
  everything.

None of them may change `input`: the caller's list is theirs.

| call | answer |
|---|---|
| `FILTER.execute(List.of("a", "bb", "ccc"), s -> s.length() > 1)` | `[bb, ccc]` |
| `SORT.execute(List.of(3, 1, 2))` | `[1, 2, 3]` |
| `DISTINCT.execute(List.of("pear", "fig", "pear", "apple"))` | `[pear, fig, apple]` |
| `REVERSE.execute(List.of(1, 2, 3))` | `[3, 2, 1]` |
