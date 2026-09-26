The page's most dangerous mistake is **swallowing `InterruptedException`**: an
empty `catch` loses the interrupt for good. Its "meaningful handling" answer is
a polling wait that treats an interrupt as giving up: catch the exception,
**restore the flag** with `Thread.currentThread().interrupt()`, and return. It
also warns that `Thread.interrupted()` **clears** the flag as a side effect.

Write `Poller.waitUntil(BooleanSupplier condition, long pollMillis)`:

- Check `condition`. If it holds, return `true`. Otherwise sleep `pollMillis`
  milliseconds and check again, for as long as it takes.
- If the thread is interrupted, stop waiting and return `false`, with the
  thread's interrupt flag **set** when the method returns, so the caller can
  still see it.

| situation | answer |
|---|---|
| the condition holds on its third check | `true` |
| another thread interrupts the caller while it sleeps | `false`, and the caller's flag is set |
| the caller was already interrupted, and the condition does not hold | `false`, and the caller's flag is still set |
