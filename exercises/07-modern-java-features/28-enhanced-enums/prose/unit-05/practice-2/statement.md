The help desk's tickets follow the support team's workflow:

| From | May move to |
|---|---|
| `NEW` | `ASSIGNED`, or `CLOSED` (spam) |
| `ASSIGNED` | `IN_PROGRESS`, or back to `NEW` |
| `IN_PROGRESS` | `RESOLVED`, or back to `ASSIGNED` |
| `RESOLVED` | `CLOSED`, or back to `IN_PROGRESS` when the customer reopens it |
| `CLOSED` | nothing |

Today the workflow lives only in a wiki page. Last week an agent's script moved a spam ticket
from `CLOSED` straight to `IN_PROGRESS`, and the customer got a "we're working on it" email for a
message they never sent. The agent's screen shows one button per allowed next state.

Finish the four `TODO`s in `src/main/java/practice/Ticket.java`: the workflow table in
`transitions()`, `canMoveTo(...)` and `allowedNext()` on `Ticket.State`, and `Ticket.moveTo(...)`.

When you're done, the workflow above is the only way a ticket moves, every state answers both
questions, and nothing a screen does with its buttons changes the workflow. About twelve lines in
total.
