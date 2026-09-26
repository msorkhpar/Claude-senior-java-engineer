*Fail fast*: validate the arguments at the start of a method and throw at once,
before any state has changed. Write `Thermostat`:

- `Thermostat()` starts with target `20` and an empty history.
- `void setTarget(Integer celsius)` accepts a target from `5` to `30`
  inclusive: it becomes the target and is appended to the history.
  - a `null` target throws `NullPointerException` with the message `celsius`
    (`Objects.requireNonNull` does this in one call);
  - a target out of range throws
    `IllegalArgumentException("Target out of range: <celsius>")`.
- `int getTarget()` and `List<Integer> history()` (the accepted targets, oldest
  first). Whether the returned list is a copy or a view is up to you; the tests
  only read it.

| calls | `getTarget()` | `history()` |
|---|---|---|
| `setTarget(22)` | `22` | `[22]` |
| `setTarget(22); setTarget(42)` | `22` (the second call throws) | `[22]` |

Once a `throw` runs, nothing after it in the method runs, so where you put the
checks decides what a failed call leaves behind.
