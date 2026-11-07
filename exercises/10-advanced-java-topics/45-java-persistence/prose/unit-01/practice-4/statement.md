The help desk's agents open tickets all day, each from a session of their own, and many tickets
share a title such as `password reset`. Agents report that a ticket they just opened shows no
tags, while a colleague's ticket opened at the same moment shows them, and that a new
`password reset` ticket sometimes files its tags under one from last week.

`open(...)` in `src/main/java/practice/Tickets.java` inserts the ticket, then files the tags under
the id it gets back. Write `insertTicket(connection, title)`: insert the ticket row and return the
id the database gave that row. The id column is an identity column; the database chooses the
value. About eight lines.

| before | call | afterwards |
|---|---|---|
| empty table, ids start at 7001 | `open(c, "laptop will not boot", [hardware, urgent])` | returns 7001; 7001 has both tags |
| a colleague's ticket is inserted while this one is being opened | `open(c, "mailbox full", [mail, quota])` | the returned id is the "mailbox full" ticket, and it has both tags |
