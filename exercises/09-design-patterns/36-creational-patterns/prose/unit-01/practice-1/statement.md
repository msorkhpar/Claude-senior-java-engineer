A lazy Singleton creates its object on first use. The page's warning is that
two threads calling `getInstance()` for the first time at once can each see
"not created yet" and each build one. **Double-checked locking** fixes it:
check without the lock, then take the lock and **check again** before
creating, with **the field holding the value declared `volatile`** (a field
of type `T`), so no thread sees a half-built object.

Write `Lazy<T>`, the same idea for any value:

- `Lazy.of(supplier)` returns a holder and **creates nothing yet**.
- `get()` calls the supplier on the first call and returns that object from
  every call after it.
- Even when threads race on the first `get()`, **the supplier runs once** and
  every thread gets the same object.

| calls | supplier runs | result |
|---|---|---|
| `Lazy.of(s)` | 0 | a holder |
| `get()`, `get()` | 1 | the same object twice |
| thread A and thread B call `get()` together | 1 | the same object in both |
