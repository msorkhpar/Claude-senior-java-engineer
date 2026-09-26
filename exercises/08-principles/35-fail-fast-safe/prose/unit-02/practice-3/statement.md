A listener registry is read on every event and written only when a listener joins or leaves: the
read-heavy, write-rare work `CopyOnWriteArrayList` is made for. Its iterator walks the array that
was current when the iterator was created, so it never throws `ConcurrentModificationException`,
and it never sees a write made after it started. The page's example:

```java
// Listener registry -- read-heavy, perfect for CopyOnWriteArrayList
CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
listeners.add(new MyListener()); // Rare
// Fire event to all listeners -- very frequent, lock-free iteration
for (EventListener listener : listeners) {
    listener.onEvent(event); // No ConcurrentModificationException possible
}
```

Write `EventBus<E>` with `subscribe(Consumer<E>)`, `unsubscribe(Consumer<E>)` and
`publish(E event)`, which hands the event to every subscribed listener in the order they
subscribed. A listener may subscribe or unsubscribe listeners, itself included, while it is
handling an event, and then:

- **nothing throws**: a listener that subscribes another during `publish` breaks nothing;
- **an event in flight goes to the listeners subscribed when its `publish` began**: a listener
  subscribed during `publish` gets only later events, and a listener that leaves during
  `publish`, or is removed by another listener during it, still gets that event and no later
  one; no other listener misses it;
- **each `subscribe` is one subscription**: a listener subscribed twice hears each event twice,
  and one `unsubscribe` removes one of its subscriptions.

The tests grade what one thread shows: publishing while listeners join and leave. Many threads
publishing and subscribing at once is why the page picks `CopyOnWriteArrayList`, and it is not
graded.
