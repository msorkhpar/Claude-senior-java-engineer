For an exception thrown very often whose stack trace nobody reads, the
protected four-argument constructor of `RuntimeException`,
`(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)`,
can switch the trace off. Write the unchecked `ValidationException` with two
constructors that both switch off suppression **and** the stack trace:

- `ValidationException(String message)` (no cause)
- `ValidationException(String message, Throwable cause)`

| expression | result |
|---|---|
| `new ValidationException("bad").getMessage()` | `"bad"` |
| `new ValidationException("bad").getStackTrace().length` | `0` |

"No stack trace" should hold for the exception's whole life, not only right
after construction, and switching the trace off must not cost the cause.
