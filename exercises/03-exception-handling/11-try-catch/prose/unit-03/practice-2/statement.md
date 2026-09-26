The `finally` block runs after the `try` and `catch` blocks whether an exception was thrown or
not, and even when the `try` block exits with `return`.

Write `Flow.run(IntSupplier body, List<String> log)`. It records what runs, in order, in `log`:

- `"try"` as the `try` block starts, then it evaluates `body` and returns its value;
- when `body` throws an `IllegalStateException`, `"catch " + message`, and `run` returns `-1`;
- `"finally"` from a `finally` block.

Any other exception from `body` is not caught: it leaves `run`.

| body | returns | log |
|---|---|---|
| throws `IllegalStateException("boom")` | `-1` | `["try", "catch boom", "finally"]` |
| returns `7` | `7` | `["try", "finally"]` |

Page Q2's own example is a `try` that returns while a `finally` still prints. Check every way out
of your method.
