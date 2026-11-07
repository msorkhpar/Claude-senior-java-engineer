Version 2 of the company's audit library adds `appendAll(batch)` to the `AuditTrail` interface.
Twelve teams implement `AuditTrail` in their own code, written against version 1: an in-memory
trail for tests, one that writes to a file, one that forwards to a message queue. None of them
will change their classes for this release.

The payments service records each transfer as a batch, the debit and then the credit, on its
`LockedTrail`. A compliance report reads `entries()` on its own thread, and an auditor once
flagged a debit with no credit in a report.

Finish the two `TODO`s in `src/main/java/practice/AuditTrails.java`:

- `AuditTrail.appendAll`, which every team's trail gets without changing a line;
- `LockedTrail.appendAll`.

Keep `append` and `entries` as they are. About four lines in total.
