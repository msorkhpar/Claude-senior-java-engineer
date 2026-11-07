The payment service records each payment's time as an `Instant` taken from the system clock, and
hands an audit library a copy made with `Date.from(...)`. Every night a job reads the copies back
and lists the payments whose audit copy is missing or was edited. Since the service moved to a
newer host, the job lists almost every payment, and nobody has touched the audit data.

`AuditReconciler.changed(...)` already walks the payments. Finish the two `TODO` methods in
`src/main/java/practice/AuditReconciler.java`:

- `asStored(recorded)`: the time the audit library holds once it has been given `recorded`;
- `matches(recorded, copy)`: whether the copy is still exactly what the library was given.

When you're done, an untouched copy never shows up in the list, and a copy that someone moved,
even by a single millisecond, always does. About two lines in total.
