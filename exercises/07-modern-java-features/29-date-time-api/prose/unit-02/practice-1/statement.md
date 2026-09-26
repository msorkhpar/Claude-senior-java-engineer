Every `java.time` class is **immutable**: `plusDays`, `with(...)` and the rest
never change the object they are called on, they return a new one. The API is
also **null-hostile**: it throws `NullPointerException` for a null argument
instead of accepting it. Write your own value class the same way.

Write `Invoice`, an immutable invoice:

- `Invoice(LocalDate issued, int termDays)` creates it. A `null` issue date
  is refused right here with a `NullPointerException`.
- `LocalDate issued()` and `int termDays()` return what it was created with.
- `LocalDate dueDate()` is `issued` plus `termDays` days; if that day is a
  Saturday or a Sunday, the due date is the **following Monday**.
- `Invoice extendedBy(int days)` returns an invoice with the same issue date
  and a term `days` longer. The invoice it is called on does not change.

| invoice | `dueDate()` |
|---|---|
| `new Invoice(LocalDate.of(2024, 3, 1), 14)` | `2024-03-15` (a Friday) |
| the same, `.extendedBy(4)` | `2024-03-19` |
| `new Invoice(LocalDate.of(2024, 3, 1), 15)` | `2024-03-18` (the 16th is a Saturday) |

`TemporalAdjusters` has an adjuster for "the next Monday".
