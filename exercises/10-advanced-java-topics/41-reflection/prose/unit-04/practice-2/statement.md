Two classic reflection slowdowns: **looking members up in a loop**, and
**calling `setAccessible(true)` on every call**. Frameworks avoid both by
scanning a class **once**, preparing its members (made accessible then) and
caching them per class, so each later read is just `field.get(bean)`.

Write `PropertyReader`. Its constructor takes a `scanner` that returns a
class's fields (normally `Class::getDeclaredFields`); the tests watch it.

- `read(bean, property)` returns the value of the field named `property` of
  `bean`'s class (any access level).
- The first read of a class asks the scanner for its fields **once**, makes
  **each of them accessible once, right then**, and caches them by name. Later
  reads of that class, on any bean, use the cache.
- The cache is **per class**: two classes with a field of the same name each
  read their own.
- An unknown property is an `IllegalArgumentException`.

| calls | scanner calls for `Person` | result |
|---|---|---|
| `read(ann, "name")` | 1 | `"Ann"` |
| then `read(ann, "age")`, `read(bob, "name")` | still 1 | `41`, `"Bob"` |
| `read(rex, "name")`, `rex` a `Pet` | 1 for `Pet` | `"Rex"` |
| `read(mug, "label")`, `read(pen, "label")` for `Shop.Item` and `Store.Item` | 1 each | `"mug"`, `"pen"` |
| `read(ann, "salary")` | | `IllegalArgumentException` |
