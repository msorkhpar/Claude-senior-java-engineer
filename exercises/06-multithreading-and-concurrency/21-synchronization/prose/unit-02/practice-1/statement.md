The page warns that `volatile int[] arr` makes only the **reference** volatile:
`arr[0] = 42` is an ordinary write. For element-level guarantees it points to
`AtomicIntegerArray`. And a read-modify-write such as `scores[i] += x` is a
compound action that `volatile` never makes atomic.

Write `ScoreBoard`, a board of `size` score slots that many threads update:

- `int update(int slot, IntUnaryOperator change)` applies `change` to the slot's
  score and returns the new score. Two updates of the **same** slot never lose
  one another's result. `change` has no side effects, so it may be called again
  on a fresh value.
- An update of one slot never waits for an update of a **different** slot.
- `int get(int slot)` reads one score; `int[] snapshot()` copies all of them.

| calls, from `new ScoreBoard(3)` | answer |
|---|---|
| `update(0, x -> x + 5)` | `5` |
| `update(2, x -> x + 1)`, then `snapshot()` | `1`, then `[5, 0, 1]` |
| slot 1 is 0; one thread adds 10 while another adds 1 | `get(1)` is `11` |
