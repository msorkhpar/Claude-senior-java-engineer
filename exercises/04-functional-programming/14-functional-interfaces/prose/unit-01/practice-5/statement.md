`Consumer<Integer>` boxes every int it sees. The primitive specializations take the primitive directly:
`IntConsumer` is `void accept(int)`, and `ObjIntConsumer<T>` is `void accept(T, int)`, handy when an element
travels with its index.

Write two methods in `Indexed`:

1. `forEachIndexed(List<T> items, ObjIntConsumer<T> action)` calls `action` once per element, in order, with the
   element and its zero-based position.
2. `histogram(int[] values, int buckets)` returns an `int[buckets]` where `counts[v]` is how many times `v` occurs
   in `values`. Do the counting with an `IntConsumer` passed to `IntStream.forEach`.

| call | result |
|---|---|
| `forEachIndexed(["Alice", "Bob"], (s, i) -> …)` | `(Alice, 0)`, `(Bob, 1)` |
| `histogram([1, 1, 2, 0], 3)` | `[1, 2, 1]` |

Lists may repeat an element; values may fall outside `0 … buckets-1`.
