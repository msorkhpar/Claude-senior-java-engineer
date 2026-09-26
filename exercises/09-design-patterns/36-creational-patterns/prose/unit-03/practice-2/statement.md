The page's `ImmutableConfig` shows how a builder produces an **immutable**
object: `final` fields, no setters, and **defensive copies** of the builder's
collections, so the product never changes after `build()`, whatever happens to
the builder or to the lists callers get back.

Write `ImmutableConfig` with `host()`, `port()`, `ssl()`, `allowedOrigins()`
and `properties()`, and its `Builder` (`ImmutableConfig.builder()`) with
`host`, `port`, `ssl`, `addOrigin`, `property` and `build()`:

- Defaults: host `localhost`, port `8080`, ssl `false`, no origins, no
  properties.
- `build()` refuses a port outside `0..65535` with `IllegalStateException`;
  `port(...)` itself accepts any number, so a later `port(...)` can fix it.
- Adding to the builder after `build()` does not change a built config, and
  the config's own list and map cannot be modified.

| chain | result |
|---|---|
| (nothing) | `localhost`, `8080`, `false`, `[]`, `{}` |
| `.host("api.example.org").ssl(true).addOrigin("https://a.example.org")` | those values, origins `[https://a.example.org]` |
| `.port(65535)` / `.port(65536)` | accepted / `IllegalStateException` from `build()` |
| `.port(70000).port(443)` | port `443` |
