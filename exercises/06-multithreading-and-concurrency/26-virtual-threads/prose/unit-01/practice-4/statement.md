Virtual threads are cancelled the same way as platform threads: by
**interruption**. A virtual thread blocked in `sleep()` or `BlockingQueue.take()`
throws `InterruptedException` as soon as it is interrupted. The page's pitfall is
the handler that swallows it: the loop keeps going and the thread can no longer be
cancelled. The fix is to stop, and to restore the interrupt status with
`Thread.currentThread().interrupt()` so the caller can still see it.

Write `Worker.drain(BlockingQueue<String> in, Consumer<String> sink)`. It runs on
the calling thread (the tests run it on a virtual thread):

- It takes items from `in`, waiting while the queue is empty, and hands each one
  to `sink`, in queue order.
- It keeps going until its thread is **interrupted**, then returns how many items
  it handed to `sink`.
- It returns with the thread's **interrupt status set**.

| queue | then | answer |
|---|---|---|
| `a`, `b`, `c` put in order | | `sink` gets `a`, `b`, `c` |
| empty | the thread is interrupted while `drain` waits | `drain` returns `0` at once |
| `a`, `b` | interrupted after both are handed on | returns `2`; `Thread.currentThread().isInterrupted()` is `true` |
