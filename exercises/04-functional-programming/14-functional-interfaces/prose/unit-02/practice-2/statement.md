`optional.orElse(compute())` runs `compute()` every time, because Java evaluates a method's arguments before the
call. `orElseGet(supplier)` calls the supplier only when the `Optional` is empty, and `Optional.or(supplier)`
(Java 9+) does the same for a fallback that is itself an `Optional`.

Write `Lookup.resolve(Optional<T> primary, Supplier<Optional<T>> secondary, Supplier<T> fallback)`. It returns the
primary's value if present, else the secondary's value if present, else `fallback.get()`.

| primary | secondary | fallback | result |
|---|---|---|---|
| `Optional.of("cache")` | `Optional.of("db")` | `"default"` | `"cache"` |
| empty | `Optional.of("db")` | `"default"` | `"db"` |
| empty | empty | `"default"` | `"default"` |

The secondary lookup and the fallback may be expensive (a database call, an HTTP request): no work may be done
that the answer does not need, and each is asked at most once. Whatever the fallback returns is the answer, even
`null`.
