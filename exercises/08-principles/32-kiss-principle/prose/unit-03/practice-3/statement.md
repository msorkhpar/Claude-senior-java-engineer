The page's last interview question designs a notification system for concurrent use, and
keeps it simple: a `CopyOnWriteArrayList` of listeners, iterated directly. No event bus, no
priorities, no asynchronous dispatch. Use that list, as the page does; the tests cannot race
threads, so they check the behaviour below rather than which list holds the listeners.

Write the generic `Notifier<T>`:

- `addListener(Consumer<T> listener)` registers a listener. Registering the same listener
  twice registers it twice, so it hears each event twice;
- `publish(T event)` hands the event to every listener, in the order they were added. With
  no listeners it does nothing.
- **A listener may add another listener while an event is being published.** That must not
  throw `ConcurrentModificationException`, and the new listener hears the next event, not the
  current one (iteration over a `CopyOnWriteArrayList` sees a snapshot, which gives both).
- **A listener that throws is not hidden**: that very exception, not a wrapper around it,
  propagates to the caller of `publish`, the simple and predictable behaviour the page asks
  for, and the listeners after it are not called.

Example: with listeners adding `"1:" + event` and `"2:" + event.toUpperCase()` to a list,
`publish("hello")` leaves `["1:hello", "2:HELLO"]`.
