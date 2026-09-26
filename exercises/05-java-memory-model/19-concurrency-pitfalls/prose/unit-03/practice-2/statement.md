The page's `CorrectSingleton` shows double-checked locking done right: a
**volatile** field, one read of it into a local, a lock only when it is still
`null`, and a second check under the lock. Without `volatile`, the reference
may be published before the constructor finishes. Without the second check,
two racing threads both build the object. Taking the lock on every call is
correct but gives up the point of the pattern.

Write the generic `Lazy<T>`, built with a `Supplier<T> factory`:

- `get()` returns the value, calling `factory.get()` the first time only,
  **exactly once** even when several threads make that first call together.
  A factory that returns `null` makes `get()` throw `NullPointerException`.
- Once the value exists, `get()` returns it **without taking any lock**.
- `isInitialized()` says whether the value exists yet.

| calls | factory calls | result |
|---|---|---|
| `get()` | 1 | the new value |
| `get()`, `get()`, `get()` | 1 | the same value each time |
| two threads call `get()` together, first time | 1 | both get the same value |
