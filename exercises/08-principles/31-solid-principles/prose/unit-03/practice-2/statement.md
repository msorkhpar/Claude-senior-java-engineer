In concurrent code, a client must be able to swap one thread-safe queue for another
without changing its assumptions. The page's `TaskQueue` contract is:

- `offer(item)` adds the item and returns `true`, or **returns `false`** when it cannot
  accept it; it does not throw when full;
- `poll()` returns and removes the head, or **returns `null`** when the queue is empty;
- `size()` is the current count, and `isEmpty()` says whether it is 0;
- both reject a `null` item **the same way**, with `NullPointerException`.

In `Queues`, write both implementations and a client:

- `BoundedTaskQueue(capacity)`: `offer` returns `false` when it is at capacity (valid per
  the contract). The page backs it with an `ArrayBlockingQueue`;
- `UnboundedTaskQueue`: `offer` always returns `true`, however many items it holds (also
  valid: it exceeds the minimum guarantee). The page backs it with a
  `ConcurrentLinkedQueue` for thread safety; the tests here check the contract only;
- `drainAll(queue)`, written against `TaskQueue` only, takes every item, head first.

The tests use one thread, so they check the contract's answers, not thread safety: which backing
queue you pick for that, and whether `drainAll` polls until `null` or counts `size()` first, is
not graded.

A bounded queue of capacity 2 accepts `"a"` and `"b"` and answers `false` for `"c"`.
