The page's second pitfall hand-writes a consumer with `wait` and `notify`:

```java
// VIOLATION: Manual wait/notify
synchronized (queue) {
    while (queue.isEmpty()) {
        queue.wait();
    }
    return queue.remove(0);
}
```

Replace it with a `BlockingQueue`, which does all of that waiting for you. Write the generic
`Handoff<T>`:

- `Handoff(int capacity)` holds at most `capacity` items.
- `produce(T item)` adds an item at the back. **When the hand-off is full, it waits** until
  a consumer makes room: it neither drops the item nor lets the hand-off grow past its
  capacity.
- `consume()` removes and returns the oldest item. **When the hand-off is empty, it waits**
  for an item: it never returns `null`.
- `produce` and `consume` declare `InterruptedException`: **a wait that is interrupted
  throws it** to the caller, rather than returning `null` or quietly dropping the item.
- `next()` works like `consume()` but declares no checked exception. The page's rule for
  thread interruption applies: when the wait is interrupted, **restore the interrupt flag**
  with `Thread.currentThread().interrupt()` and throw an `IllegalStateException` whose cause
  is the `InterruptedException`.
- `size()` is the number of items waiting.

Example: after `produce("item1")`, `produce("item2")`, `produce("item3")`, three calls to
`consume()` return `"item1"`, `"item2"` and `"item3"`, in that order.
