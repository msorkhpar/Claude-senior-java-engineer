The page's `ArrayList` grows only when its array is **completely full**, and
then to about 1.5 times its length: `newCapacity = oldCapacity + (oldCapacity >> 1)`,
so 10 becomes 15, 22, 33, 49, 73, 109. The JDK adds one rule the formula
misses: a list always grows by **at least one** slot, so an array of length 0
or 1 still grows. And `new ArrayList<>()` is lazy: its array has length 0
until the first `add`, which allocates the default 10 slots.

Write two methods of `Growth` that list the array lengths a list goes through,
starting with its initial length, while `elements` elements are added one by
one:

- `capacities(int initialCapacity, int elements)` for `new ArrayList<>(initialCapacity)`;
- `defaultCapacities(int elements)` for `new ArrayList<>()`.

| call | answer |
|---|---|
| `capacities(10, 16)` | `[10, 15, 22]` |
| `capacities(10, 10)` | `[10]` |
| `capacities(1, 6)` | `[1, 2, 3, 4, 6]` |
| `defaultCapacities(0)` | `[0]` |
| `defaultCapacities(11)` | `[0, 10, 15]` |
