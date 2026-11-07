Every night a partner shop's orders are imported in one transaction. Now and then the partner
sends a line the database refuses: a missing SKU, or one longer than twelve characters. The
warehouse has started finding orders in its pick lists that have some of their lines but not all,
and the partner is asking why a few orders never arrive at all.

`importAll(...)` in `src/main/java/practice/OrderImport.java` already runs the night in one
transaction and commits once. Finish `importOne(...)`, which writes one order with `insertOrder` and
`insertLine` and says whether the order made it into the import.

When you're done, an order the database refuses any part of is left out entirely, and every other
order of the night is imported with all its lines. About eight lines.

The tests import into an in-memory H2 database and check what a second connection sees after the
commit.
