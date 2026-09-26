An `enum` with a single constant is the page's preferred Singleton: the JVM
creates `INSTANCE` once, keeps it the same through serialization and refuses to
build another by reflection. What the enum does **not** protect is the state
inside it.

Write `ConnectionPool` as an enum with the constant `INSTANCE` and:

- `addConnection(String url)` adds a connection; `null` or a blank string is
  refused with `IllegalArgumentException` and changes nothing.
- `connections()` returns the connections in the order added, as a list the
  caller **cannot modify**.
- `clear()` empties the pool (the tests use it to start clean).

| calls | `connections()` |
|---|---|
| `addConnection("jdbc:h2:mem:a")`, `addConnection("jdbc:h2:mem:b")` | `[jdbc:h2:mem:a, jdbc:h2:mem:b]` |
| `connections().add("x")` | `UnsupportedOperationException` |
| `addConnection("  ")` | `IllegalArgumentException` |
