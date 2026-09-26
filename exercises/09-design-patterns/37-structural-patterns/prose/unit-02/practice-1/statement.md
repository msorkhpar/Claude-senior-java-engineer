The page's notifiers: an `EmailNotifier` is the component, and SMS and Slack
are decorators that implement the same `Notifier` interface, delegate to the
notifier they wrap and then add their own part. The page's first pitfall is a
decorator that forgets to delegate, so **every method delegates to the wrapped
notifier**, `getDescription()` as much as `send()`.

Given: `Notifier` and `EmailNotifier`. Write:

- `NotifierDecorator`, an abstract base that keeps the wrapped notifier and
  passes both methods on. **A `null` wrappee is refused in the constructor**
  with `NullPointerException`.
- `SmsDecorator(wrappee, phone)`: `send` returns the wrapped result followed by
  `" | SMS to <phone>: <message>"`; the description adds `" + SMS(<phone>)"`.
- `SlackDecorator(wrappee, channel)`: adds `" | Slack #<channel>: <message>"`
  and `" + Slack(#<channel>)"`.

Decorators stack in any combination, and **the same decorator applied twice
adds its part twice**.

| notifier | `send("alert")` |
|---|---|
| `Email("a@example.org")` | `Email to a@example.org: alert` |
| `Sms(Email, "+1")` | `Email to a@example.org: alert \| SMS to +1: alert` |
| `Slack(Sms(Email, "+1"), "ops")` | `... \| SMS to +1: alert \| Slack #ops: alert` |
| `Sms(Sms(Email, "+1"), "+2")` | `... \| SMS to +1: alert \| SMS to +2: alert` |

| notifier | `getDescription()` |
|---|---|
| `Slack(Sms(Email, "+1"), "ops")` | `Email(a@example.org) + SMS(+1) + Slack(#ops)` |
