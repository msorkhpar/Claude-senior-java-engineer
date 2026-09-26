A security setting that callers can change after it was checked is not a
setting. The page's `SecureConfig` makes **defensive copies**: the constructor
**copies** the list it is given, so the caller's later changes do not leak in,
and the getter returns a list that **cannot be used to change** the config.
`List.copyOf` does both, and it also refuses `null` elements.

Write `CorsConfig`:

- `new CorsConfig(origins)` keeps its own copy of `origins`; a `null` list or
  a `null` origin is refused with `NullPointerException`.
- `origins()` returns the origins in the order given.
- `allows(origin)` says whether `origin` is one of them (by value).

| action | `allows("https://evil.example.org")` afterwards |
|---|---|
| `list.add("https://evil.example.org")` on the list passed in | `false` |
| `config.origins().add("https://evil.example.org")` (if it does not throw) | `false` |

| built with | result |
|---|---|
| `[https://app.example.org, https://admin.example.org]` | `origins()` in that order |
| `[https://app.example.org, null]` | `NullPointerException` |
