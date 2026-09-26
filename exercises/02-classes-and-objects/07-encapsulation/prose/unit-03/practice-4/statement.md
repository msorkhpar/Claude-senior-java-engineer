For a class with many attributes, the page recommends the **builder pattern**: a
nested builder collects the values one call at a time, and `build()` checks them
once and makes an object that cannot change afterwards.

Write `ServerConfig` and its nested `ServerConfig.Builder` (from
`ServerConfig.builder()`):

- `host(String)`: required. `build()` throws `IllegalStateException` when no host
  was given or it is blank.
- `port(int)`: optional, `8080` when not given. A port outside `1..65535` is
  refused at once with `IllegalArgumentException`.
- `tag(String)`: optional, may be called many times; tags keep their order.
- `build()` returns the `ServerConfig`.

`ServerConfig` has `host()`, `port()` and `tags()`. It has no setters and no public
constructor, and nothing done to the builder after `build()` changes a config that
was already built.

Examples:

```
ServerConfig c = ServerConfig.builder().host("db.example.org").tag("eu").tag("primary").build();
c.host()   -> "db.example.org"
c.port()   -> 8080
c.tags()   -> [eu, primary]
ServerConfig.builder().port(8081).build()   -> IllegalStateException
ServerConfig.builder().port(0)              -> IllegalArgumentException
```
