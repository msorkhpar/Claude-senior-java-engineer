The page's lazy singleton checks `if (instance == null)` and then creates the
instance. It warns that this is **not thread-safe**: two threads can both see
`null` and both create an object.

Write the general tool behind a safe lazy singleton, `Lazy<T>`:

- `Lazy(Supplier<T> supplier)` remembers how to make the value, and makes
  **nothing** yet;
- `get()` returns the value, creating it with the supplier on the first call only.
  Every later call returns the same object.
- `get()` is safe when several threads call it at once: while one thread is still
  running the supplier, another thread calling `get()` must **wait** for that value
  rather than run the supplier again. The supplier runs **exactly once**, and every
  thread gets that one object.

You may assume the supplier never returns `null`.

**Examples**

```
calls = 0
lazy = new Lazy<>(() -> { calls++; return new Object(); })
calls              -> 0
a = lazy.get(); b = lazy.get()
a == b             -> true
calls              -> 1
```
