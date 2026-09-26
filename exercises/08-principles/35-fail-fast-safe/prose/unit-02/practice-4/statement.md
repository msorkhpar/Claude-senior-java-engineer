`ConcurrentHashMap` refuses `null` keys and values. In a concurrent map, `get(key)` returning
`null` must mean one thing only, "not there", because checking `containsKey` first and then
calling `get` is two steps another thread can come between. The page's pitfall and its fix:

```java
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
map.put("key", null);  // NullPointerException!
map.put(null, "value"); // NullPointerException!

// FIX -- use sentinel values or Optional
```

and its example of the `Optional` form:

```java
ConcurrentHashMap<String, Optional<String>> mapWithOptional = new ConcurrentHashMap<>();
mapWithOptional.put("key", Optional.empty()); // Represents "mapped but no value"
mapWithOptional.put("key2", Optional.of("value")); // Represents "mapped with value"
```

Write `Settings`, shared by many threads and backed by a `ConcurrentHashMap`:

- `set(key, value)` records the setting, and `get(key)` returns `Optional.of(value)`;
  `isSet(key)` is `true` once a key is set;
- **a setting may be present with no value**: `set("proxy", null)` records it, so
  `isSet("proxy")` is `true` and `get("proxy")` is `Optional.empty()`;
- `unset(key)` forgets the setting, so `isSet` is `false` again; a key never set is not set,
  and `get` of it is `Optional.empty()`;
- a value is kept exactly as given: `set("k", "")` reads back as `Optional.of("")`, and
  `" v "` as `Optional.of(" v ")`; only `null` means "no value";
- keys follow the map's own rule: `set(null, ...)` throws `NullPointerException`.

The map is a `ConcurrentHashMap` because many threads share it, and a single `get` on it
cannot be split by another thread the way `containsKey`-then-`get` can. That thread-safety
is the page's reason for the design; the tests check the map is a `ConcurrentHashMap` and grade
the behaviour above, which one thread shows.
