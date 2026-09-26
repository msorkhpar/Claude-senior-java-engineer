The "enum with Class token" pattern keeps everything an enum gives you
(`EnumSet`, `valueOf`, `switch`) and adds partial type awareness: each constant
stores the `Class` of its values. The page's pitfall is a generic getter that
just does `(T) value`: erasure turns that cast into nothing, so a wrong type is
not caught where it happens but later, somewhere else. Its fix is a `cast()`
that uses the token.

Complete the enum `ConfigKey` (constants, fields and constructor are given):

- `<T> T cast(Object value)`: returns `value` when it is an instance of the
  constant's `type()`, and the constant's default value when `value` is
  `null`. Any other value throws `ClassCastException` **inside `cast()`**, so
  even a caller that ignores the result sees the failure.
- `static Optional<ConfigKey> fromKey(String key)`: the constant whose `key()`
  equals `key`, or empty.

| call | answer |
|---|---|
| `MAX_CONNECTIONS.cast(25)` | `25` |
| `SERVER_HOST.cast(null)` | `"localhost"` |
| `MAX_CONNECTIONS.cast("ten")` | throws `ClassCastException` |
| `RATE_LIMIT.cast(100)` | throws `ClassCastException` (an `Integer` is not a `Double`) |
| `fromKey("enable.ssl")` | `Optional[ENABLE_SSL]` |

Key texts come from configuration files, so the text passed to `fromKey` is not
the same `String` object as a constant's key.
