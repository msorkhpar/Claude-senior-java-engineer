**High-level modules should not depend on low-level modules; both should depend on
abstractions.** Without that, a `NotificationManager` creates an `EmailSender` itself, and
switching to SMS means editing the manager. The page's fix:

```java
interface MessageSender {
    void send(String recipient, String message);
}

class NotificationManager {
    private final MessageSender sender;
    NotificationManager(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender);
    }
    void notifyUser(String user, String msg) {
        sender.send(user, msg);
    }
}

var manager = new NotificationManager(new EmailSender());
// Or swap to SMS without changing NotificationManager
var smsManager = new NotificationManager(new SmsSender());
```

In `Notify`, write the two details and the manager:

- `EmailSender` records each message as `"EMAIL[<recipient>]: <message>"` and
  `SmsSender` as `"SMS[<recipient>]: <message>"`, with the recipient and the message exactly
  as given (no trimming, no change of case), one entry per send, repeats included;
  `sentMessages()` lists them in order;
- `NotificationManager(sender)` takes its sender through the constructor, rejects `null`
  there with `Objects.requireNonNull`, and `notifyUser(user, message)` sends through it.

The manager must work, unchanged, with **any** `MessageSender`, including one written
after it, such as a push sender or a test's fake.
