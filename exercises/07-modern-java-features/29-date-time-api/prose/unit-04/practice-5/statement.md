Instant arithmetic is bounded: `Instant.MAX.plusSeconds(1)` throws a
`DateTimeException`, and a duration of `Long.MAX_VALUE` seconds overflows the
`long` of epoch seconds itself, which throws an `ArithmeticException`. The fix
the page names is to check the bounds.

A token is issued at an instant and lives for a `Duration`. Some tokens are
given an enormous time to live to mean "never expires". Write `Expiry`:

- `static Instant expiresAt(Instant issued, Duration ttl)` returns
  `issued + ttl`. When that would run past the end of the timeline, it
  returns `Instant.MAX` instead of throwing. A negative `ttl` is refused with
  an `IllegalArgumentException`; a zero `ttl` is allowed and expires at once.
  Every nanosecond of `ttl` counts.
- `static boolean isExpired(Instant issued, Duration ttl, Clock clock)` says
  whether the token has expired at the clock's current instant. A token is
  expired **from** its expiry instant on.

| call | answer |
|---|---|
| `expiresAt(2024-03-15T12:00:00Z, 30 minutes)` | `2024-03-15T12:30:00Z` |
| `expiresAt(2024-03-15T12:00:00Z, Duration.ofSeconds(Long.MAX_VALUE))` | `Instant.MAX` |
| `isExpired(12:00Z, 30 minutes, clock at 12:29:59Z)` | `false` |
| `isExpired(12:00Z, 30 minutes, clock at 12:30:00Z)` | `true` |
