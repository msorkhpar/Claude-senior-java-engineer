This practice continues the ticket store: the starter is the previous practice's solution, with
two new methods for the agents' ticket list, `stateLabel` and `countIn`.

The list now meets rows its release can't read. During a rolling deploy, servers running the
release after this one already write `ESCALATED` while this release still serves the list, and
tickets imported from the old help-desk tool have no state at all. Today one such row makes the
whole list fail.

Finish `tryRead` in `src/main/java/practice/TicketStore.java`, which both methods call for every
row. When you're done, a row this release can't read shows as `unknown` and is left out of the
counts, and every other row, `WAITING` included, reads as before. About six lines in total.
