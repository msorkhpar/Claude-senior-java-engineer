A constructor reference (`ArrayList::new`, `StringBuilder::new`) is a factory: each call
of the interface's method runs the constructor and returns a **new** object. The
interface decides which constructor: a `Supplier` calls the no-argument one, a
`Function<String, T>` a one-argument one. An array constructor reference
(`String[]::new`) is the `IntFunction` that `Stream.toArray` needs for a typed array.

Write three methods in `Factories`:

1. `<C> List<C> buckets(int count, Supplier<C> factory)` returns `count` objects made by
   `factory`.
2. `<T> List<T> build(List<String> names, Function<String, T> factory)` makes one object
   per name, in order: a repeated name gets its own new object each time.
3. `String[] shout(List<String> words)` returns the words upper-cased, as a `String[]`.

| call | result |
|---|---|
| `buckets(3, ArrayList::new)` | three empty `ArrayList`s |
| `buckets(2, TreeSet::new)` | two empty `TreeSet`s |
| `build(["Alice", "Bob"], StringBuilder::new)` | builders holding `"Alice"` and `"Bob"` |
| `shout(["a", "b"])` | `{"A", "B"}` |

The buckets are meant to be filled separately afterwards.
