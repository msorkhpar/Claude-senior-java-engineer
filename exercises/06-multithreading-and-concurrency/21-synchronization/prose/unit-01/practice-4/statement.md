An import splits a file into numbered batches and processes them on a pool of workers, in
parallel. The batches must still be committed to the database in batch order, because each one
continues the running totals of the one before. A worker that has finished batch `n` calls
`awaitTurn(n)`, commits, and calls `done(n)`.

Since the sequencer went in, two things have shown up in the logs: a batch committed while the
batch before it was still committing, and, on another day, an import that stopped with every
worker idle and a finished batch waiting for a turn that had already come.

Write `awaitTurn(...)` and `done(...)` in `src/main/java/practice/CommitSequencer.java`, on the
monitor of its `lock` field. A worker waits without using the processor until its batch's turn,
and whatever order the workers start waiting in, each batch goes ahead exactly when the one before
it is done. About eight lines in total.
