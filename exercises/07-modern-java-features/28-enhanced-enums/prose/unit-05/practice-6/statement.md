The page's last use case combines an enum with a generic record: the enum
`ResultStatus { SUCCESS, FAILURE, PENDING }` says what happened, the record
`Result<T>(ResultStatus status, T value, String message)` carries the payload.
Expected failures then travel as values, and `map`/`flatMap` chain steps
without nested `try`/`catch`. The page's edge case: `map()` on a failure
propagates the failure **without invoking the mapper**.

The starter gives the enum, the record and its factories (`success`,
`failure`, `pending`). Write the record's methods:

- `<R> Result<R> map(Function<T, R> mapper)`: on a success, a success holding
  `mapper.apply(value)`. Otherwise a result with the same status and message,
  no value, and the mapper is not called.
- `<R> Result<R> flatMap(Function<T, Result<R>> mapper)`: on a success, the
  result the mapper returns. Otherwise as for `map`.
- `Optional<T> getValue()`: the value, or empty.

| call | answer |
|---|---|
| `success("42").map(Integer::parseInt).map(n -> n * 2)` | `SUCCESS`, value `84` |
| `Results.<String>failure("not found").map(Integer::parseInt)` | `FAILURE`, message `"not found"`; the mapper never runs |
| `Results.<String>pending().map(String::length)` | `PENDING` |
| `success("x").flatMap(s -> failure("bad input"))` | `FAILURE`, message `"bad input"` |
