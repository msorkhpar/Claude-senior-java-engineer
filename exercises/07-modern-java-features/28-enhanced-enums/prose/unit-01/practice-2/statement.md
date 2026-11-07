The support desk keeps each ticket's state in a text column of the tickets table. The next
release adds two states, `TRIAGED` and `WAITING`, and the table already holds a few hundred
thousand rows written by the current one (`PreviousState` in the starter). Nobody wants to
migrate them.

Finish `write` and `read` in `src/main/java/practice/TicketStore.java`. `write` turns a state of
either release into the column's text; `read` turns that text back into a `State` of the new
release.

When you're done, every row the previous release wrote loads as the state it was saved as, and
every state of the new release, `WAITING` included, survives a save and a load. About two lines
in total.
