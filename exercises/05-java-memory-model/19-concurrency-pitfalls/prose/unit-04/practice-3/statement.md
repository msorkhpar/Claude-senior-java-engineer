The page's `WideLock` runs a 500 ms computation while holding its lock, so
every other thread waits. Its fix: **compute outside, mutate inside**. Hold
the lock only for the state change. And a method that returns internal state
returns a **defensive copy**, or callers change it outside the lock.

Write `ResultLog`, a thread-safe log of results:

- `processAndStore(String input, UnaryOperator<String> transform)` computes
  `transform.apply(input)` **without holding the lock**, then appends the
  result to the log under the lock, and returns it.
- `results()` returns the log in the order results were stored, as a list
  the caller owns: changing it does not touch the log, and later stores do
  not show up in it.

| calls | `results()` |
|---|---|
| `processAndStore("a", String::toUpperCase)`, then `processAndStore("b", s -> s + "!")` | `[A, b!]` |
| `results().add("x")`, then `results()` | still `[A, b!]` |
| thread 1's `transform` is still running | thread 2's `processAndStore` still finishes |
