The notification service delivers webhooks to customers' endpoints from an outbox, on a pool
that every deploy shuts down with `shutdownNow()`. Two complaints reached the team in the same
week. Deploys wait for the notification pool until their timeout runs out. And during one deploy
a customer's endpoint, which was down at the time, received a burst of identical requests within
a few milliseconds.

`deliver(...)` already retries correctly. Finish the two `TODO`s in
`src/main/java/practice/WebhookSender.java`:

- `run()`: the sender's life on its pool thread;
- `backOff(...)`: the pause between two attempts.

When you're done, a deploy ends the sender whether it is waiting for the next payload or pausing
before a retry, and no request goes to the endpoint after the deploy began. About ten lines in
total.

The tests run the sender on a real `ExecutorService`. The pause they hand it doesn't wait, but it
answers interrupts exactly as `Thread.sleep` does.
