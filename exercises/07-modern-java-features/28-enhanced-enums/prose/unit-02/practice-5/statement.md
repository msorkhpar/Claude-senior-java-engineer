The page's fifth workaround, the Supplier-based pattern, gives each enum
constant a `Supplier<?>` instead of a value. The value is made when it is asked
for, so a constant can hand out a **new** mutable default each time, which a
single stored value cannot. The page's rule of thumb: use it when values vary
per call, not when one stable, cached value is wanted.

Complete the enum `LazyDefault` (the constants and their suppliers are given):

- `<T> T generate()` returns a value made by the constant's supplier, made
  anew on every call.
- `<T> T generateAs(Class<T> type)` does the same, but checks the value with
  `type` and throws `ClassCastException` inside the method when it does not
  fit.

| call | answer |
|---|---|
| `List<String> a = EMPTY_LIST.generate()` | `[]` |
| `a.add("x")`, then `EMPTY_LIST.generate()` | `[]` again, a different list |
| `EMPTY_MAP.generateAs(Map.class)` | `{}` |
| `EMPTY_LIST.generateAs(Map.class)` | throws `ClassCastException` |
