The page's `NotificationFactory` is a **creator**: its `notify(message)` calls
the abstract **factory method** `createNotification()` and sends through
whatever product comes back. Each subclass decides the product, so adding a new
channel means adding a subclass and **never editing the creator**.

`Notification` (given) has `String send(String message)`. Write:

- `NotificationFactory.notify(message)`: create the product through
  `createNotification()` and return what its `send` returns. A factory method
  that returns `null` is a design error: throw `IllegalStateException`.
- `EmailFactory`, whose product sends `"Email: " + message`.
- `SmsFactory`, whose product sends `"SMS: " + message`.

| creator | `notify("hi")` |
|---|---|
| `new EmailFactory()` | `Email: hi` |
| `new SmsFactory()` | `SMS: hi` |
| any new subclass, e.g. push | what its own product returns |
| a subclass returning `null` | `IllegalStateException` |
