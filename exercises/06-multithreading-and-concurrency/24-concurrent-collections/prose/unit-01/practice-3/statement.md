`ConcurrentHashMap` refuses `null` keys **and** `null` values: `put("key", null)`
throws `NullPointerException`. The page explains why: in a concurrent map,
`get` returning `null` must mean "absent", because a `containsKey` check made
afterwards could already be stale. Its fix is to store a sentinel instead of
`null`, and the page uses the empty string for that.

Write `NullableMap`, a thread-safe string map built on the given
`ConcurrentHashMap<String, Object>` that **does** accept a `null` value:

- `put(String key, String value)` stores `value`, which may be `null`. A `null`
  key is refused with `NullPointerException`.
- `get(String key)` returns the stored value, or `null` when the value is
  `null` or the key is absent.
- `containsKey(String key)` says whether the key was stored, even with a
  `null` value.

The tests use one thread, so they read the map's answers, not its thread safety; build on the
`ConcurrentHashMap` anyway, as the page does. Pick your sentinel with care: every `String` a caller stores, the empty
string included, must read back as itself.

| calls | answer |
|---|---|
| `put("host", "db1")`, `get("host")` | `"db1"` |
| `get("port")`, `containsKey("port")` | `null`, `false` |
| `put("proxy", null)`, `get("proxy")`, `containsKey("proxy")` | `null`, `true` |
| `put("suffix", "")`, `get("suffix")` | `""` |
| `put("word", "null")`, `get("word")` | `"null"` |
| `put(null, "x")` | throws `NullPointerException` |
