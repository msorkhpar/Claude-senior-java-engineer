An enum cannot extend another enum, so the page's fix for "I need more
severities" is a shared interface: `StandardSeverity` and
`ExtendedSeverity` both implement `Severity`, and code that works with
`Severity` accepts constants of either enum.

The starter declares the interface and both enums. Write two methods that work
only through `Severity`:

- `static List<Severity> all()` returns every constant of both enums,
  ordered by `level()` from lowest to highest. The caller must not be able to
  change the list.
- `static Optional<Severity> highest(Collection<? extends Severity> among)`
  returns the constant with the highest `level()`, or an empty `Optional` when
  `among` is empty.

| call | answer |
|---|---|
| `all()` | `[TRACE, LOW, MEDIUM, HIGH, CRITICAL, CATASTROPHIC]` |
| `highest(List.of(LOW, CRITICAL, MEDIUM))` | `Optional[CRITICAL]` |
| `highest(List.of(HIGH, TRACE))` | `Optional[HIGH]` |
| `highest(List.of())` | `Optional.empty` |
| `all().add(LOW)` | throws `UnsupportedOperationException` |

Note that `ExtendedSeverity` has a constant *below* every standard one.
