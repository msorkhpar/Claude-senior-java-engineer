The loyalty service imports partner files into its points ledger on a few worker threads, one
file at a time. Two incidents last month:

- A café partner sent a file that would have taken a member below zero. The file was rejected, as
  it should be, and from then on no other file was imported until someone restarted the service.
- During a deploy, the service waited out its whole shutdown timeout: two workers were waiting for
  the ledger behind a long airline import, and the interrupts the deploy sent them changed nothing.

Write `importFile` in `src/main/java/practice/PointsLedger.java`. It applies the file with the
`applyAll` method that is already there, holding the ledger's lock. A rejected file must leave the
ledger ready for the next one, and its `IllegalArgumentException` must still reach the caller. A
worker that is interrupted while it waits for the ledger must stop waiting, import nothing, and
end with `InterruptedException`. About five lines.

The tests run the workers on threads of their own and hold the ledger's lock themselves to keep
a worker waiting.
