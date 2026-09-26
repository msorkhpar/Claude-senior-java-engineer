`Function<T, R>` declares no checked exceptions, so a reference to a method that
`throws IOException` does not compile as a `Function`. The page's fixes are a custom
functional interface that declares the exception, and a wrapper that catches it.

`IoFunction<T, R>` is given: its `apply` may throw `IOException`. Write two methods in
`Unchecked`:

1. `<T, R> Function<T, R> function(IoFunction<T, R> fn)` returns a `Function` that calls
   `fn`. An `IOException` is rethrown as an `UncheckedIOException` carrying it as the
   cause.
2. `<T, R> List<R> mapAll(List<T> items, IoFunction<T, R> fn)` applies `fn` to every item,
   in order; an `IOException` from any item surfaces exactly as it does from `function`.

With `static String load(String key) throws IOException` in scope, both of these compile
and work:

| call | result |
|---|---|
| `function(Loader::load).apply("a")` | `"value of a"` |
| `mapAll(List.of("a", "b"), Loader::load)` | `["value of a", "value of b"]` |

Wrap only what the standard interface cannot carry; anything else the method throws
should reach the caller as it was thrown.
