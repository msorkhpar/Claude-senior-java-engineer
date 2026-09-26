A logging decorator adds behaviour **around** the call it delegates: one entry
before, one after. The page's best practices also say to expose the log for
testing, but unmodifiable, so nobody can rewrite it.

Given: `Notifier`. Write `LoggingDecorator implements Notifier`:

- `send(message)`:
  - logs `"Sending: <message>"`; **Sending is logged before the call is
    delegated**, so it stays in the log even when the wrapped notifier throws
    (the exception reaches the caller unchanged);
  - delegates, then logs `"Sent: <result>"`, where **the Sent entry records what
    the wrapped notifier returned**;
  - returns that result unchanged.
- `getDescription()` is `"Logging(" + wrapped description + ")"`.
- `getLog()` returns the entries in order; **the log cannot be changed through
  `getLog()`**.

| wrapped `send("hi")` | `send("hi")` returns | `getLog()` |
|---|---|---|
| returns `"Email: hi"` | `"Email: hi"` | `[Sending: hi, Sent: Email: hi]` |
| throws `IllegalStateException` | throws it | `[Sending: hi]` |
