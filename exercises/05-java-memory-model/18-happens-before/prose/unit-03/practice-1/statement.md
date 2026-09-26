The page's Q6 walks through a broken `UnsafeStack`: `elements` and `size` are shared
mutable state, `pop()` is a **check-then-act** and `size++` / `--size` are
**read-modify-write** actions. Its checklist says every read **and** write must be
protected by the same lock; guarding the writes alone still leaves a data race.

Write `SafeStack<T>`, a stack of up to any number of items, backed by an array that
starts with 16 slots and doubles when full:

- `push(T item)` adds an item on top;
- `pop()` removes and returns the top item, or throws `java.util.EmptyStackException`
  when the stack is empty (the size stays `0`);
- `size()` returns how many items it holds.

Every method, reads included, holds the stack's own monitor (`this`), so a caller can
also hold `synchronized (stack)` around a compound action of its own.

| calls | answer |
|---|---|
| `push(1)`, `push(2)`, `push(3)`, then `pop()` three times | `3`, `2`, `1` |
| `pop()` on an empty stack | `EmptyStackException`, and `size()` is `0` |
| 100 pushes | `size()` is `100` |
