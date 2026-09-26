Whether an exception is checked should follow from whether the client can
reasonably be expected to recover from it. `Gateway` has two failures; give
each exception the right superclass (`Exception` or `RuntimeException`) and
write the method.

- `RateLimitedException(String message, int retryAfterSeconds)` with
  `getRetryAfterSeconds()`: the caller sent too much and may simply try again
  later.
- `BadRequestIdException(String message)`: the caller passed no request id,
  which is a programming error.
- `String send(String requestId, int callsThisMinute)`:
  - a `null` or blank id (blank as `String.isBlank()` means it: empty or only
    Unicode whitespace) throws `BadRequestIdException("Request id is required")`;
  - more than `10` calls this minute throws
    `RateLimitedException("Rate limit exceeded", 60)`;
  - otherwise returns `"sent " + requestId`.

  When both failures apply, which one is reported is up to you.

| call | result |
|---|---|
| `send("r1", 1)` | `"sent r1"` |
| `send("r1", 11)` | throws `RateLimitedException`, retry after `60` |
| `send("", 1)` | throws `BadRequestIdException("Request id is required")` |
