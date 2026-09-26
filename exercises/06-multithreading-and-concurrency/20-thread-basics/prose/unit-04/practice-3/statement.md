A thread in `Object.wait()` is `WAITING`: it has released the monitor and
waits for a signal. But it "can wake up spuriously without being notified",
which is why the page says `wait()` belongs **in a loop that re-checks the
condition**. The same loop makes a signal that came before the wait harmless.

Write `Gate`, which uses **the `Gate` object itself** as its monitor:

- `void pass()` returns at once if the gate is open. While it is closed, the
  thread waits on the gate's monitor, and it returns only once the gate is
  open. It declares `throws InterruptedException`.
- `void open()` opens the gate for good and wakes every waiting thread.

| situation | result |
|---|---|
| `pass()` while the gate is closed | the thread is `WAITING` |
| another thread calls `open()` | the waiting thread returns from `pass()` |
| two threads wait, then `open()` | both return from `pass()` |
| a thread is woken while the gate is still closed | it waits again |
| `open()`, then `pass()` | `pass()` returns at once |
