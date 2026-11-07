The quoting desk keeps its rates in a `Book` that isn't thread-safe. The design gives the book to
one thread, the desk's own executor, so that nothing needs a lock: every call into the book runs
there. Quotes come from the market-data adapters as `CompletableFuture`s. When an adapter has the
rate cached, the future is already complete; otherwise one of the adapter's own threads completes
it later.

The desk passed every test, where the adapters are stubs with every rate cached. In production its
book has twice lost a rate during a busy minute, and a thread dump caught an adapter thread inside
`Book.apply`.

Finish the two `TODO`s in `src/main/java/practice/FxDesk.java`:

- `onQuote(...)`: put the quote into the book once it has arrived, and return at once;
- `rate(...)`: answer with the book's rate for a pair.

When you're done, every call into the book runs on the desk's thread, whether the quote was already
complete, arrives later from an adapter, or is being read. About three lines in total.

The tests give the desk a single-thread executor named `desk` and a book that records which thread
made each call.
