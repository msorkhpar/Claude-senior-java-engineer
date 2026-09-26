An event bus keeps one listener list per event type, so publishers and
listeners only share type names. The page's answer to memory leaks is a
**subscription handle**: `subscribe` returns an object whose `cancel()`
removes that listener again.

`Subscription` (given) has `void cancel()`. Write `EventBus`:

- `subscribe(type, listener)` adds `listener` (a `Consumer<Object>`) to
  `type`'s list and returns a `Subscription`. **`cancel()` removes only that
  subscription**: the same listener subscribed to another type stays there.
- `unsubscribe(type, listener)` removes it too.
- `publish(type, data)` calls `accept(data)` on each listener of `type`.
- **A type with no listeners is a no-op** for `publish`, `unsubscribe` and
  `listenerCount` (which returns `0`).
- **A blank event type** (`null`, empty or only spaces) **is refused** by
  `subscribe` and `publish` with `IllegalArgumentException`; so is a `null`
  listener.
- `eventTypes()` returns the types that have been subscribed to; **the set
  a caller gets cannot change the bus**.

| calls | effect |
|---|---|
| L1 on `"user.created"`, L2 on `"order.placed"`; `publish("user.created", "ada")` | L1 gets `"ada"`, L2 nothing |
| L on `"a"` and `"b"`, cancel the `"a"` subscription; publish to both | L gets only the `"b"` event |
| `publish("nobody.listens", 1)` | nothing |
| `subscribe("  ", L)` | `IllegalArgumentException` |
