A catch clause names types, not patterns, but inside the catch block Java 21 lets you inspect the
caught exception with a pattern-matching `switch`.

Write `ExceptionClassifier.describe(Runnable action)`. It runs `action` and returns `"ok"` when it
completes. When it throws a `RuntimeException`, it returns a description of it:

| thrown | description |
|---|---|
| a `NumberFormatException` | `"not a number: <message>"` |
| any other `IllegalArgumentException` | `"bad argument: <message>"` |
| an `ArithmeticException` | `"arithmetic: <message>"` |
| any other `RuntimeException` | `"unexpected <SimpleClassName>: <message>"` |

`<SimpleClassName>` is what `getClass().getSimpleName()` returns (a nested type has no outer prefix).
Anything that is not a `RuntimeException` reaches the caller unchanged.

For example, an action throwing `new IllegalStateException("closed")` is described as
`"unexpected IllegalStateException: closed"`.

Look up where these exception types sit in the hierarchy before you decide the order of the cases.
