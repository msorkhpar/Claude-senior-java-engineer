The page warns about **cascading updates**: observer A reacts to an event by
changing the subject, which notifies again, which reaches A again, and the
loop never ends. Its fix is a flag: while a notification is running, a new
`publish` is ignored, and the flag is reset in a `finally` block.

`Observer` (given) has `void update(String event)`. Write `GuardedSubject`:

- `addObserver(o)` registers `o`.
- `publish(event)` calls `update(event)` on every observer, in the order
  they were added, and returns `true`.
- **A `publish` made while a notification is running** (from any observer,
  at any point of the loop) **is ignored** and returns `false`.
- An exception thrown by an observer leaves `publish` unchanged (it is not
  caught), but **the flag is cleared anyway**, so the next `publish` works.

| observers | call | result |
|---|---|---|
| R1, R2 | `publish("temp")` | both get `"temp"`, `true` |
| R1, A, R2 where A publishes `"adjust"` | `publish("temp")` | R1, A, R2 get only `"temp"`; A's call returns `false` |
| X throws on `"bad"` | `publish("bad")`, then `publish("ok")` | the exception, then `"ok"` is delivered, `true` |
