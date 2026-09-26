Standard functional interfaces such as `Function` and `Consumer` declare no
checked exceptions, so a lambda body that throws one does not compile. The page
shows two ways out: handle the failure inside the lambda, or accept a lambda of
your own interface that *may* throw and wrap it into a plain `Function`.

Write two static methods in `SafeParser`:

1. `parseAll(List<String> texts)` parses each text with `Integer.parseInt` and
   returns the numbers in order, skipping every text that is not a valid number.
2. `unchecked(CheckedFunction<T, R> function)` returns a `Function<T, R>` that
   calls `function`. A **checked** exception it throws comes out as a
   `RuntimeException` whose cause is that exception; an **unchecked** exception
   comes out unchanged. `CheckedFunction` is given, nested in `SafeParser`.

| call | answer |
|---|---|
| `parseAll(["1", "2", "3"])` | `[1, 2, 3]` |
| `parseAll(["1", "abc", "3", "xyz", "5"])` | `[1, 3, 5]` |
| `unchecked(s -> s.length()).apply("four")` | `4` |

Think about what a caller needs to see when the wrapped function fails.
