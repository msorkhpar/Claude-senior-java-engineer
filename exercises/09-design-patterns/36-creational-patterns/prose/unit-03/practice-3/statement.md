The page's `HttpRequest` puts the **required** values, the URL and the method,
in the `Builder`'s **constructor**, so a builder without them can never exist;
headers and a body are optional fluent steps. It also warns about **reusing a
builder**: building twice from one builder must not let the second build's
changes leak into the first request.

Write `HttpRequest` (`url()`, `method()`, `headers()`, `body()`) and its static
nested `Builder`:

- `new HttpRequest.Builder(url, method)` throws `NullPointerException` at once
  if either is `null`.
- `header(name, value)` and `body(text)` return the builder.
- `build()` returns a request whose `headers()` is **a read-only map** that
  later builder calls never change; `body()` is `Optional.empty()` when unset.

| chain | request |
|---|---|
| `new Builder("https://api.example.org", "POST").header("Content-Type", "application/json").body("{}")` | POST, 1 header, `Optional[{}]` |
| `new Builder(null, "GET")` | `NullPointerException` |
| `b.header("A","1")` → build `r1`; `b.header("B","2")` → build `r2` | `r1` has `A` only, `r2` has `A` and `B` |
