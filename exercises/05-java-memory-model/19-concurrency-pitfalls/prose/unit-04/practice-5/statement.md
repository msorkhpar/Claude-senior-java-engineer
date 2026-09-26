Listeners are read on every event and changed rarely: the page's case for
`CopyOnWriteArrayList`. Every write copies the array, and every iteration
walks a **snapshot**, so a listener may subscribe or unsubscribe while an event
is being delivered, with no `ConcurrentModificationException` and no lock.
The page also warns that `Collections.unmodifiableList` is only a read-only
**view**: the list behind it can still change. `List.copyOf` is a real copy.

Write `EventBus`, where a listener is a `Consumer<String>`:

- `subscribe(listener)` and `unsubscribe(listener)` (which returns whether it
  was subscribed) change the listeners.
- `fire(event)` calls every listener subscribed when the delivery **starts**,
  in subscription order. A listener may subscribe or unsubscribe listeners,
  itself included, from inside its call: the current delivery goes on over
  the listeners it started with.
- `listeners()` returns the current listeners as an unmodifiable copy that
  never changes afterwards.

| during `fire("e1")` | result |
|---|---|
| listener A unsubscribes itself | no exception; B still gets `"e1"` |
| listener A subscribes C | C does not get `"e1"`, and gets `"e2"` |
