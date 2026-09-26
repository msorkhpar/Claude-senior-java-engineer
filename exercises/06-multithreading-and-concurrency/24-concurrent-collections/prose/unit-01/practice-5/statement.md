The page's `ConcurrentSkipListSet` is a thread-safe **sorted** set: it always
iterates in sorted order, whatever order the elements arrived in, and it
supports `NavigableSet` range operations such as `subSet`, `headSet` and
`tailSet`, `first` and `last`. Note that those range operations return **views**
backed by the set, and that `subSet`'s bounds can each be inclusive or not.

Write two methods over a `ConcurrentSkipListSet<String>`:

- `Ranks.between(set, from, to)` returns, as a **new** sorted set that later changes
  to the set do not affect, the names `n` with `from <= n <= to`, in sorted
  order. Both bounds are **inclusive**.
- `Ranks.ends(set)` returns `[first, last]`, or an empty list when the set is
  empty, and leaves the set as it was.

| set | call | answer |
|---|---|---|
| `{banana, apple, date, cherry}` | `between(set, "apple", "c")` | `[apple, banana]` |
| `{banana, apple, date, cherry}` | `between(set, "apple", "cherry")` | `[apple, banana, cherry]` |
| `{banana, apple, date, cherry}` | `ends(set)` | `["apple", "date"]` |
| `{}` | `ends(set)` | `[]` |
