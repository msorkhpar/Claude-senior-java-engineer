The page's `PiggybackingExample` writes plain fields first and a `volatile`
flag last. A reader that sees the flag set is guaranteed to see every write
made before it: the plain writes **piggyback** on the one volatile write.

Write `ReportBoard`, a one-shot board that one writer thread publishes to
and any number of reader threads read from.

- `publish(String title, int total)` stores the report. The board is written
  **once**: a second `publish` throws `IllegalStateException` and changes
  nothing. A `null` title throws `NullPointerException`.
- `read()` returns `Optional.of(new Report(title, total))` once the board is
  published, and `Optional.empty()` before.

Keep `title` and `total` in plain (non-volatile) fields and the "published"
state in one `boolean` flag that makes them visible to readers. Write the
data first and the flag last; read the flag first.

| calls | `read()` |
|---|---|
| none | `Optional.empty()` |
| `publish("Q3 sales", 1250)` | `Optional[Report[title=Q3 sales, total=1250]]` |
| `publish("Q3 sales", 1250)`, then `publish("Q4", 7)` | the second call throws; still the Q3 report |
