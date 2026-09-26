The page's pitfall 2 and Q3 explain how a subtask's failure travels:
`join()` and `invoke()` **rethrow the subtask's `RuntimeException` unwrapped**,
the same type, not an `ExecutionException`. But when the subtask failed **in
another thread**, the exception you catch may be a **copy** of that type whose
`getCause()` is the original, so its own message may not be the original's.
The page reads the original with `e.getCause() != null ? e.getCause() : e`.
(In a pool with several workers a copy can itself be copied at a later join,
so following `getCause()` for as long as it is still a `NumberFormatException`
is the safe way to reach the original. The tests run `total` in a pool of one
worker, called from the test's own thread.)

Write `ParseSum.total(pool, items, threshold)`: a `RecursiveTask<Long>` run in
`pool` that parses every string of `items` with `Long.parseLong` and sums them,
splitting at the midpoint while a range is longer than `threshold`. A leaf that
meets an item that is not a number throws

    new NumberFormatException("item " + i + " is not a number: " + items[i])

Do not catch it inside the tasks. `total` catches the `NumberFormatException`
that comes out of the pool and throws `IllegalArgumentException` with the
**original** message.

| items | threshold | answer |
|---|---|---|
| `"1"`, `"2"`, ..., `"10"` | 2 | `55` |
| `"1"`, `"2"`, `"3"`, `"4"`, `"5"`, `"6"`, `"x"`, `"8"` | 2 | `IllegalArgumentException("item 6 is not a number: x")` |
| no items | 2 | `0` |
