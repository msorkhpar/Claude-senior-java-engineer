The deploy notifier tells billing, search, the audit trail and a few others that a new version
went out. They subscribe at startup and hardly ever after, while deploy events arrive all day from
several threads. Two new subscribers change the list from inside a delivery: a one-shot reminder
unsubscribes itself after its first deploy, and the canary watcher subscribes the full-rollout
watcher once the canary passes.

Finish the two `TODO` methods in `src/main/java/practice/DeployNotifier.java`:

- `newSubscribers()`: the list that holds the subscribers;
- `deploy(event)`: delivers the event to every subscriber that was subscribed when the call
  began, in the order they subscribed, and returns how many it delivered to.

A subscriber added during a delivery hears from the next deploy on; one removed during a delivery
still hears the deploy in progress. About five lines in total. The tests use one thread.
