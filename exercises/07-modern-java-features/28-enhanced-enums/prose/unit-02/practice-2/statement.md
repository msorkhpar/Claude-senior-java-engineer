A generic "enum" of keys is only half a configuration system: the values need
somewhere to live. The page pairs the keys with a **type-safe heterogeneous
container** (Effective Java, Item 33): the map stores values as `Object`, and
each key's `Class<T>` token casts them back, so callers never cast.

The starter declares the key type, a record, and two keys:

```java
public record Key<T>(String name, Class<T> type, T defaultValue) { }
public static final Key<Integer> PORT = new Key<>("port", Integer.class, 8080);
public static final Key<String> HOST = new Key<>("host", String.class, "localhost");
```

Write the container's methods:

- `<T> void put(Key<T> key, T value)` stores `value` under `key`. A `null`
  value throws `NullPointerException`. The value is checked with the key's
  `Class` token, so a value of the wrong type, which only a raw `Key` can pass,
  throws `ClassCastException` and is not stored.
- `<T> T get(Key<T> key)` returns the stored value in `T`, or the key's
  `defaultValue()` when nothing was stored.
- `boolean contains(Key<?> key)` and `int size()`.

| calls on a new `TypedConfig` | answer |
|---|---|
| `put(PORT, 9090)`, then `get(PORT)` | `9090` (an `Integer`) |
| `get(HOST)` with nothing stored | `"localhost"` |
| `put((Key) PORT, "eighty")` | throws `ClassCastException` |
| `put(HOST, null)` | throws `NullPointerException` |

Keys are records, so a key built again from the same parts is **equal** to the
original, though not the same object, and must find the same value. A key
with the same name but another type is a different key.
