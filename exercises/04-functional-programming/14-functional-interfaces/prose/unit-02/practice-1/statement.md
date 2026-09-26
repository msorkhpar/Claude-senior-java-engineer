A `Supplier` takes nothing and returns a value, and every `get()` runs its lambda again, so a supplier that keeps
state can hand out a different value on each call. `IntSupplier` does the same with `int getAsInt()`, and no
boxing.

Write two methods in `Sources`:

1. `counter(int start, int step)` returns an `IntSupplier` that yields `start`, `start + step`, `start + 2*step`, …
2. `cycling(List<T> items)` returns a `Supplier<T>` that yields the items in order and starts over after the last.

| source | successive calls |
|---|---|
| `counter(0, 1)` | `0, 1, 2, …` |
| `counter(10, -5)` | `10, 5, 0, …` |
| `cycling(["red", "green"])` | `red, green, red, …` |

A program may hold several sources at once. An empty list has nothing to cycle through: throw
`IllegalArgumentException("items must not be empty")`. Watch what the supplier captures: a `List` the caller still
holds is a mutable object.
