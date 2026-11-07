The billing service delivers its internal events through `EventBus`. Handlers subscribe to a
type: the audit log to `Auditable`, the dunning job to `InvoiceEvent`, a debug tap to `Object`.
Since the last refactoring the audit log stays empty, and the startup check warns that nobody
handles `InvoicePaid`, although the dunning job does.

`publish` and `unhandled` already loop over the subscriptions. Finish the two `TODO`s in
`src/main/java/practice/EventBus.java`:

- `accepts(type, event)`: whether a handler subscribed to `type` receives this event;
- `handles(type, eventClass)`: the same answer for a class, before any event exists.

A message the decoder can't map is published as `null`; it must reach no handler and must not
throw. Two lines in total.
