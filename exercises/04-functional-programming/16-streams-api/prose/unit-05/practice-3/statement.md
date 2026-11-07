The bank's monthly statements are built from the ledger, which hands its lines over as a stream in
posting order. For the large business accounts the month has millions of lines, so the stream is
parallel. A customer reads a statement from top to bottom and checks it against their own books,
and the statement starts with the account's opening line: its first posting of the month.

Finish the two methods in `src/main/java/practice/Ledger.java`:

- `statements(...)`: each account's lines, in the order they were posted;
- `openingLines(...)`: each account's first posted line.

Both must give the same answer for a sequential and for a parallel stream. About two lines in
total.

The tests hand the methods parallel streams in which some threads get to later lines first.
