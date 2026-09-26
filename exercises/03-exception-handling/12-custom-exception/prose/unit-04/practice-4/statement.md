Sometimes a checked exception is caught and wrapped in an unchecked one, so it
can travel up the stack without forcing every caller to handle it. Write
`Unchecked.get(CheckedSupplier<T> supplier)`, which returns
`supplier.get()` and translates a failure:

| supplier throws | `get` throws |
|---|---|
| nothing, returns `42` | returns `42` |
| `IOException("disk")` | `UncheckedIOException("disk")`, cause the `IOException` |
| any other checked exception, e.g. `SQLException("x")` | `RuntimeException("Translated exception")`, cause the original |
| an unchecked exception | that same exception |

The order of your `catch` clauses decides which one sees an exception first.
