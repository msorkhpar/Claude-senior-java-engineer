The billing overview lists every invoice with its lines. It loads them with
`loadAllSlowly()`, and the database team has traced one overview to 341 statements. The
numbers are right; the round trips are the problem.

Write `loadAll()` in `src/main/java/practice/InvoiceRepository.java`. It returns what
`loadAllSlowly()` returns: every invoice in id order, including invoices that have no lines
yet, each one once, with its lines in line-number order. It may send the database only one
statement, however many invoices there are. About fifteen lines in total.

The tests run the repository against H2 in memory through a connection that counts the
statements it executes.
