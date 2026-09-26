The page's `MutableConfig` lets a reader see a new host with an old port.
Its fix: an immutable record for the configuration, and a store that swaps
in a whole new record at once, so every reader gets a consistent snapshot.

Write `ConfigStore` and its nested record
`AppConfig(String host, int port, boolean ssl, int maxRetries)`:

- `AppConfig`'s compact constructor refuses a `null` host
  (`NullPointerException`), a port outside `0..65535` and a negative
  `maxRetries` (`IllegalArgumentException`). `withHost` and `withPort`
  return a new config and leave the original unchanged.
- `ConfigStore(AppConfig initial)`, `get()` and `set(AppConfig next)`
  hand out and replace whole snapshots (`null` is refused).
- `update(UnaryOperator<AppConfig> change)` applies `change` to the current
  config and stores the result, **atomically**: when two threads update at
  the same time, both changes end up in the store. `change` may be called
  more than once, so it must have no side effects; return the stored config.

| calls | `get()` |
|---|---|
| `new ConfigStore(new AppConfig("localhost", 8080, false, 3))` | `AppConfig[host=localhost, port=8080, ssl=false, maxRetries=3]` |
| `update(c -> c.withPort(9090))` | port `9090`, everything else unchanged |
| thread A `update(c -> c.withHost("db"))` and thread B `update(c -> c.withPort(5432))` together | host `db` **and** port `5432` |
| `new AppConfig("h", 70000, false, 0)` | `IllegalArgumentException` |
