The page's `HttpStatus` carries three constructor parameters per constant: a
code, a reason and a category. It adds two pieces of static state, both built
after every constant exists: a code-to-constant map, so `fromCode` is O(1)
instead of a scan, and a cached copy of `values()`, because `values()` makes a
new array on every call.

The constants, fields and accessors are given. Write:

- `static Optional<HttpStatus> fromCode(int code)`: the status with that code,
  or empty, looked up in a static map built once.
- `static List<HttpStatus> all()`: every status in declaration order, served
  from the cached array. Whatever a caller does to the returned list, the next
  call to `all()` still lists every status in order.
- `static List<HttpStatus> byCategory(Category category)`: the statuses of that
  category, in declaration order.

| call | answer |
|---|---|
| `fromCode(404)` | `Optional[NOT_FOUND]` |
| `fromCode(418)` | `Optional.empty` |
| `byCategory(SUCCESS)` | `[OK, CREATED, NO_CONTENT]` |
| `all().get(0)` | `OK` |
