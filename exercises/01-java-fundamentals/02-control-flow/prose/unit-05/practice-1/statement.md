The help desk is moving its ticket routing out of a rules spreadsheet and into code. Each ticket
has a category and a severity from 1 (most urgent) to 4, and lands in one team's queue:

| Category | Queue | Who is paged |
|---|---|---|
| `BILLING`, `REFUND` | `"finance"` | nobody |
| `OUTAGE` | `"operations"` | the on-call engineer, when the severity is 1 or 2 |
| `SECURITY` | `"security"` | the security lead, always |
| `FEATURE_REQUEST`, `OTHER` | `"product"` | nobody |

A page is recorded by adding `"on-call: <ticket id>"` or `"security-lead: <ticket id>"` to the
list the router was built with.

Write `route(...)` in `src/main/java/practice/TicketRouter.java` as one `return switch`. A ticket
pages at most one person, and only the one its row names. About ten lines in total.
