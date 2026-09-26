Looking a member up (`getDeclaredMethod`) is the most expensive part of
reflection; invoking a `Method` you already hold is cheap. So frameworks
look up once and cache, keyed by `className#methodName(paramTypes)`, and call
`setAccessible(true)` once, at lookup, not on every call.

Write `MethodCache`. Its constructor takes a `Finder` (given) that does the
real lookup; the tests count its calls.

- `get(type, name, params...)` returns the method from the cache, or asks the
  finder **once** and caches it.
- The key tells apart **overloads** (the parameter types are part of it) and
  **classes whose simple names are equal** (it names the class by its full
  name).
- A returned method is **already accessible**: it was made so when it was
  looked up.
- `size()` is the number of cached methods.

| calls | finder calls | result |
|---|---|---|
| `get(Target.class, "value")` twice | 1 | the same `Method` |
| `get(Calc.class, "add", int.class, int.class)`, then `double.class, double.class` | 2 | two methods, `size()` 2 |
| `get(Shop.Item.class, "price")`, `get(Store.Item.class, "price")` | 2 | declared by `Shop.Item` and `Store.Item` |
| `get(Vault.class, "secret")`, `secret` private | 1 | invokable at once |
