Checked exceptions are for recoverable conditions, unchecked ones for
programming errors. A retry loop is where the difference shows. Write
`Retrier.withRetry(Call<T> call, int attempts)`:

- call `call.call()` up to `attempts` times and return the first result;
- a `TransientException` (checked, given) means "try again"; if the last
  attempt fails too, throw that last `TransientException`;
- an unchecked exception is not something a retry can fix: let it propagate
  immediately;
- `attempts` below `1` throws
  `IllegalArgumentException("attempts must be at least 1: <attempts>")`.

| call behaviour | `withRetry(call, 3)` | calls made |
|---|---|---|
| fails twice, then returns `"done"` | `"done"` | 3 |
| always throws `TransientException("try <n>")` | throws `TransientException("try 3")` | 3 |
| throws `IllegalStateException("bug")` | throws that `IllegalStateException` | 1 |
