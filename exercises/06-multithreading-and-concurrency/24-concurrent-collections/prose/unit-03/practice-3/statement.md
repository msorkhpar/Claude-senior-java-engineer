The on-call system hands each alert to its subscribers: the pager, the chat bridge, the SMS
gateway, the escalation timer. Subscribers change the list while they handle an alert. When the
pager reaches someone, it unsubscribes the SMS gateway, so the person isn't texted as well. The
escalation timer unsubscribes itself once an alert is acknowledged. A new audit subscriber may be
added halfway through.

The engineer on call wrote this on Monday's incident: "Paged, answered, and still got the SMS a
second later." Finish `publish(String alert)` in `src/main/java/practice/AlertBus.java`, which
keeps its subscribers in a `CopyOnWriteArrayList`:

- a subscriber unsubscribed before its turn comes doesn't get the alert;
- one subscribed during an alert starts with the next alert;
- everyone else gets it once, in the order they subscribed.

About three lines.
