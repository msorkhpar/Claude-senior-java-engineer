A command can be stored and run later. When no undo is needed, the page's
advice is to skip the command classes: a `Runnable` lambda is already a
command, and a `Queue<Runnable>` defers them.

Write `CommandQueue`:

- `enqueue(command)` adds a `Runnable` (`null` throws
  `IllegalArgumentException`); nothing runs yet. `size()` and `isEmpty()`
  report the queue.
- Commands run **first in, first out**.
- `executeNext()` removes and runs one command and returns `true`.
- `executeAll()` runs commands **until the queue is empty**, including any a
  running command enqueues, and returns how many ran.
- **An empty queue is a no-op**: `executeNext()` returns `false`,
  `executeAll()` returns `0`.
- If a command throws, the exception reaches the caller; **that command has
  already left the queue, and the rest stay queued** for a later call.

| queued | call | runs | returns |
|---|---|---|---|
| a, b, c | `executeAll()` | a, b, c | `3` |
| a, b | `executeNext()` | a | `true` (b still queued) |
| nothing | `executeNext()` | | `false` |
| x (throws), b, c | `executeAll()` | x | the exception; b, c still queued |
| a (enqueues d), b | `executeAll()` | a, b, d | `3` |
