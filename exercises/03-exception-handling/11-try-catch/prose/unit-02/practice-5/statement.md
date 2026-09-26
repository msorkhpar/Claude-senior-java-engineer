An exception thrown inside a catch block replaces the one being handled, and the original is lost.
The page's fix is a `try`/`catch` inside the catch block.

Write `Alerting.run(Runnable work, Alerter alerter)`:

- it runs `work`;
- when `work` throws a `RuntimeException`, it calls `alerter.alert(failure)` with it and then rethrows
  **that same** `failure`;
- when the alerter itself throws a `RuntimeException`, `failure` is still what is rethrown, with the
  alerter's exception attached through `failure.addSuppressed(...)`;
- anything else the alerter throws (an `Error`) is not caught: it reaches the caller.

| work | alerter | result |
|---|---|---|
| completes | (not called) | returns normally |
| throws `IllegalArgumentException("bad")` | records it | throws that `IllegalArgumentException` |
| throws `IllegalArgumentException("bad")` | throws `IllegalStateException("alert down")` | throws "bad", suppressing "alert down" |

Neither failure may disappear.
