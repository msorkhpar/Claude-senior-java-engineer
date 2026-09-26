The Strategy pattern has three parts: a **strategy interface**, the concrete
strategies, and a **context** that holds one strategy and delegates to it.
The page's first pitfall is a context whose strategy can be `null`: it fails
with a `NullPointerException` far from the mistake.

`SortStrategy<T>` (given) has `List<T> sort(List<T> data)` and `String name()`.
Write the context, `Sorter<T>`:

- `new Sorter<>(strategy)` keeps the strategy; `null` throws
  `IllegalArgumentException`.
- `sort(data)` returns what the current strategy returns for `data`.
- `setStrategy(next)` **replaces the strategy** used by every later `sort()`;
  `null` is refused with `IllegalArgumentException` here too, and the old
  strategy stays.
- `getStrategy()` returns the current strategy.

| strategy held | call | answer |
|---|---|---|
| ascending | `sort([3, 1, 2])` | `[1, 2, 3]` |
| ascending, then `setStrategy(descending)` | `sort([3, 1, 2])` | `[3, 2, 1]` |
| ascending | `setStrategy(null)` | `IllegalArgumentException` |
| none | `new Sorter<>(null)` | `IllegalArgumentException` |
