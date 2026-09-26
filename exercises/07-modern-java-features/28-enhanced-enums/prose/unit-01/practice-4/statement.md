An enum's constants are fixed when it is compiled: `new` on an enum is a
compile error, and reflection refuses to create one. When new "constants" must
arrive at run time (from a plug-in or a configuration file), the course's
workaround is a registry: named values that behave like enum constants,
but can be added later.

Write `Registry<T>`:

- `void register(String name, T value)` adds an entry. A name already taken is
  refused with `IllegalArgumentException`, and its first value stays.
- `T get(String name)` returns the value registered under `name`, and throws
  `NoSuchElementException` when there is none (never `null`).
- `List<String> names()` lists the names in **registration order**, the way
  `values()` lists constants in declaration order.
- `int size()` counts the entries.

| calls on a new `Registry<Integer>` | answer |
|---|---|
| `register("bronze", 3)`, `register("silver", 2)`, `register("gold", 1)`, then `get("silver")` | `2` |
| then `names()` | `[bronze, silver, gold]` |
| then `register("gold", 9)` | throws `IllegalArgumentException`; `get("gold")` is still `1` |
| then `get("platinum")` | throws `NoSuchElementException` |

Names reach the registry from outside (a file, a request), so a name passed to
`get` is often a different `String` object from the one registered.
