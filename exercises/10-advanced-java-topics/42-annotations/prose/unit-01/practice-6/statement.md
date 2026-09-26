`@SafeVarargs` is a promise: this generic varargs method only reads its
array and never lets it escape, so callers need no heap-pollution warning.
It fits only methods that cannot be overridden: `static`, `final`, `private`
methods and constructors. The page's safe example copies the elements into
a new list.

Write `Varargs` with two methods that **carry `@SafeVarargs`** and keep the
promise:

- `static <T> List<T> listOf(T... elements)`: the elements in order, in a list
  that **does not share the varargs array**. **`null` elements are kept.**
- `final <T> List<List<T>> group(T[]... arrays)`: one list per array, in order,
  each **a copy of its array**.

| call | result |
|---|---|
| `listOf("a", "b", "c")` | `[a, b, c]` |
| `listOf(arr)`, then `arr[0] = "z"` | the list still starts with the old element |
| `listOf("a", null)` | `[a, null]` |
| `group(new String[]{"a", "b"}, new String[]{"c"})` | `[[a, b], [c]]` |
