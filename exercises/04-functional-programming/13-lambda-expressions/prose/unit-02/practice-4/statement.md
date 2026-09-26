A lambda with no parameters must still be written with empty parentheses:
`() -> ...`. As a `Supplier`, it holds work back until someone calls `get()`,
and its body runs again on every call.

Write two static methods in `Suppliers`:

1. `valueOr(String value, Supplier<String> fallback)` returns `value` when it
   is not `null`, and otherwise the result of a fresh `fallback.get()` call,
   made every time the fallback is needed and never remembered between calls.
2. `freshLists()` returns a `Supplier<List<String>>` whose `get()` gives an
   empty, modifiable list.

| call | answer |
|---|---|
| `valueOr("a", () -> "b")` | `"a"` |
| `valueOr(null, () -> "b")` | `"b"` |
| `freshLists().get()` | `[]`, and `add("x")` works on it |

A fallback may be costly to compute, and several callers may share one list factory.
