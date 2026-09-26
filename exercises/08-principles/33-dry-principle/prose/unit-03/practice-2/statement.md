The page's pitfall: some components keep their listeners in a `CopyOnWriteArrayList`,
others in a synchronized `ArrayList`, and one in a plain `ArrayList`. Its fix is one
`SimpleEventBus` with one kind of internal thread safety: a `ConcurrentHashMap` of event
types, each holding a `CopyOnWriteArrayList` of listeners.

Complete `SimpleEventBus`:

- `subscribe(eventType, listener)` adds the listener to that type's list (subscribing the same listener
  twice subscribes it twice);
- `publish(eventType, event)` hands the event to every listener of that type, in the order
  they subscribed. **Publishing to a type with no listeners does nothing**: no exception;
- a listener may subscribe another listener **while an event is being published**. That
  publish neither fails nor calls the new listener; the next one does. A copy-on-write
  list is what makes this iteration safe;
- the tests use one thread, so they grade the bus's answers, not which thread-safe structures you use or
  whether creating a type's list is atomic; the page's `ConcurrentHashMap` and `CopyOnWriteArrayList`
  are the answer it teaches;
- `listenerCount(eventType)` is the number of listeners of that type, `0` for a type
  nobody subscribed to.

Examples:

- with listeners `a` and `b` on `"order"` and one on `"user"`, `publish("order", "o-1")`
  reaches `a` and then `b`, and not the `"user"` listener;
- `publish("nobody", "x")` does nothing, and `listenerCount("nobody")` is `0`;
- a listener that subscribes a second listener during `publish("order", "o-1")` is the only
  one called for `"o-1"`; `publish("order", "o-2")` then reaches both.
