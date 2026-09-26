A `Supplier` is a natural factory: `ArrayList::new` is a `Supplier<List<String>>` that makes a **new** list every
time it is asked. `Stream.generate(supplier)` asks it again and again, forever, so it must always be paired with
`limit(n)`.

Write `Factory.createN(int n, Supplier<T> factory)`, returning a list of `n` objects made by `factory`. Build it
with `Stream.generate` and `limit`.

| call | result |
|---|---|
| `createN(3, () -> "x")` | `["x", "x", "x"]` |
| `createN(2, StringBuilder::new)` | two empty, separate `StringBuilder`s |

A negative `n` throws `IllegalArgumentException("n must not be negative: " + n)`. Mind how many times the factory is
asked, including when the answer is empty.
