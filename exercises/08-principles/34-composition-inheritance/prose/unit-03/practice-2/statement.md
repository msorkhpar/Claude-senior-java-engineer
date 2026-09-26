Composition makes a service testable: it receives its parts through the constructor, so a
test can hand it an in-memory sender that records what was sent. The page's design:

```java
interface NotificationSender {
    boolean send(String recipient, String message);
}

interface MessageFormatter {
    String format(String template, Map<String, String> variables);
}
```

Both interfaces are given. Write `TemplateFormatter` and `NotificationService` in
`Notifications`:

- `TemplateFormatter.format` replaces **every** `${key}` with its value, and leaves a
  placeholder with no value as it is: `"Hi ${name}, bye ${name}! ${missing}"` with
  `name = Ann` is `"Hi Ann, bye Ann! ${missing}"`. A key is whatever lies between `${` and
  `}` (so `${user.name}` works), and a value is inserted exactly as it is: `$5` stays `$5`,
  and a value that looks like a placeholder, such as `${b}`, is not expanded again;
- `new NotificationService(sender, formatter)` keeps **the parts it is given**, and
  `notify(recipient, template, variables)` formats with that formatter and sends with that
  sender. With a recording sender, `notify("admin", "Alert: ${event}", {event=login})`
  sends `"Alert: login"` to `admin` and returns `true`;
- `notify` returns what the sender returned, and counts it: `getSuccessCount()` counts sends
  that returned `true`, `getFailureCount()` those that returned `false`. The page keeps both
  counts in `AtomicInteger`s so concurrent calls can share the service; the tests here check
  the counts one call at a time, and do not grade that choice.
