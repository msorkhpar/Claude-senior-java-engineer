`Instant.now()` always reads the system clock, which a test cannot control.
`Instant.now(clock)` (or `clock.instant()`) reads whatever `Clock` it is
given: `Clock.systemUTC()` in production, a fixed or hand-driven clock in a
test.

Write `AuditLog`, which stamps every action with the moment it happened:

- `AuditLog(Clock clock)` creates an empty log that reads `clock`.
- `Entry record(String action)` stores the action with the clock's current
  instant and returns the entry (`AuditLog.Entry` is a record with
  `Instant at()` and `String action()`; it is given in the starter).
- `List<String> actionsWithin(Duration window)` returns, oldest first, the
  actions recorded no earlier than `window` before the clock's **current**
  instant. An entry exactly `window` old is still inside.

| steps (clock starts at `2024-03-15T12:00:00Z`) | answer |
|---|---|
| `record("login")` | an entry at `2024-03-15T12:00:00Z` |
| clock moves 5 minutes; `record("view")` | an entry at `2024-03-15T12:05:00Z` |
| `actionsWithin(Duration.ofMinutes(10))` | `["login", "view"]` |
| `actionsWithin(Duration.ofMinutes(2))` | `["view"]` |
| clock moves 1 hour; `actionsWithin(Duration.ofMinutes(10))` | `[]` |
