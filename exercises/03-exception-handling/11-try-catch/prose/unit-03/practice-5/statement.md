If the `try` block throws and the `finally` block throws too, the exception from `finally` is the
one that propagates, and the original is lost. The page's remedy: keep the original, and when the
`finally` block has to throw, set the original as the new exception's cause with `initCause()`.

Write `Cleanup.run(Runnable work, Runnable cleanup)`. It runs `work`, then always runs `cleanup`.

| work | cleanup | result |
|---|---|---|
| completes | completes | returns normally |
| throws `W` | completes | throws `W` |
| completes | throws `C` | throws `C` |
| throws `W` | throws `C` | throws `C`, with `C.getCause() == W` |

Both are `RuntimeException`s, and a cleanup's exception is created without a cause of its own, so
`initCause` may be called on it once.
