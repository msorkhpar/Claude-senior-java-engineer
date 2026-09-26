`Function.apply` declares no checked exceptions, so a method such as one that reads a file cannot
be used as a `Function` directly. The usual answer is a custom functional interface whose one
method declares `throws Exception`, plus a helper that adapts it to a standard `Function`.

The file declares `CheckedFunction<T, R>` (method `R apply(T t) throws Exception`) and the
unchecked `ApplyFailedException(String message, Throwable cause)`. Write two static methods of
`Checked`:

1. `<T, R> Function<T, R> unchecked(CheckedFunction<T, R> function)` returns a `Function` that
   applies `function`; when it throws any exception, the `Function` throws an
   `ApplyFailedException` with the message `"failed on " + input` instead.
2. `<T, R> List<R> mapAll(List<T> inputs, CheckedFunction<T, R> function)` applies `function`
   to each input in order and returns the results, failing the same way.

| call | answer |
|---|---|
| `unchecked(Integer::parseInt).apply("42")` | `42` |
| `mapAll(List.of("1", "2"), Integer::parseInt)` | `[1, 2]` |
| `mapAll(List.of("1", "abc", "3"), Integer::parseInt)` | throws `ApplyFailedException("failed on abc")` |

Whoever catches the `ApplyFailedException` needs to know what really went wrong, and a failure
means the remaining inputs are not worth trying.
