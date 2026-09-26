Java 21's sequenced collections (JEP 431) give every collection with a defined
order the same methods: `getFirst()`, `getLast()` and `reversed()`, a reversed
**view** of the collection. `List`, `Deque`, `LinkedHashSet` and `TreeSet` are all
a `SequencedCollection`, where before each had its own way (or none) to reach
the last element. The page's edge case: `getFirst()` and `getLast()` throw
`NoSuchElementException` on an **empty** collection.

Write the class `Recent`, for any `SequencedCollection` of non-null elements in
oldest-to-newest order:

- `newestFirst(SequencedCollection<T> items, int n)` returns the last `n`
  elements, newest first, as a **new list** that later changes to `items` do not
  affect. When `n` is larger than the size, return all of them. `n` is never
  negative.
- `oldest(SequencedCollection<T> items)` returns the first element, or an empty
  `Optional` when `items` is empty.

It must work for **any** sequenced collection, not only lists.

| items | call | answer |
|---|---|---|
| `List ["a", "b", "c", "d"]` | `newestFirst(items, 2)` | `["d", "c"]` |
| same | `newestFirst(items, 10)`, `oldest(items)` | `["d", "c", "b", "a"]`, `Optional[a]` |
| `LinkedHashSet ["x", "y", "z"]` | `newestFirst(items, 2)` | `["z", "y"]` |
| an empty list | `newestFirst(items, 3)`, `oldest(items)` | `[]`, `Optional.empty` |
