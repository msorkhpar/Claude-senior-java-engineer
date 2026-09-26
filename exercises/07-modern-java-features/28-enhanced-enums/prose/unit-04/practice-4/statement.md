The page's `CollectionFactory` makes typed collections without casts: each
constant produces a different collection kind, while the generic methods keep
the element type of the caller.

```java
Collection<String> strings = CollectionFactory.ARRAY_LIST.of("a", "b", "c");
Collection<Integer> numbers = CollectionFactory.HASH_SET.of(1, 2, 3, 2, 1); // size 3
```

Fill in each constant's two methods, and write `of`:

- `<T> Collection<T> create()`: a new, empty collection of the constant's kind
  (`ARRAY_LIST` an `ArrayList`, `HASH_SET` a `HashSet`, `TREE_SET` a
  `TreeSet`), new on every call.
- `<T> Collection<T> createFrom(Collection<T> source)`: a new collection of the
  constant's kind holding `source`'s elements. It is a copy: later changes to
  `source` do not show in it.
- `@SafeVarargs public final <T> Collection<T> of(T... elements)`: a collection
  made by `create()` and filled with `elements`, so each constant keeps its own
  rules (a set drops repeats, a tree set sorts). The page explains why the
  method must be `final`.

| call | answer |
|---|---|
| `ARRAY_LIST.of("b", "a", "b")` | `[b, a, b]` |
| `HASH_SET.of(1, 2, 3, 2, 1)` | 3 elements |
| `TREE_SET.of("pear", "apple", "fig")` | `[apple, fig, pear]` |
| `ARRAY_LIST.createFrom(src)`, then `src.add("x")` | the copy has no `"x"` |
