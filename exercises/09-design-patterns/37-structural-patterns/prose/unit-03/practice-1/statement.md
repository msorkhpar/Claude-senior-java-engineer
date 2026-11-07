The fare service wraps every fare listener in a metered proxy before subscribing it, so the
dashboard can show how often each listener is called. Since metering went in, two things are
wrong. Closing a price-alert screen calls `unsubscribe` with the same reference that subscribed,
and the screen's listener keeps being told about fares. And the dashboard shows `toString`,
`equals` and `hashCode` calls next to `fareChanged`.

`Metering.metered(...)` already counts calls and passes them to the listener. Finish the two
`TODO`s in `src/main/java/practice/Metering.java`:

- `isObjectMethod(...)`: which calls the proxy answers itself instead of counting and passing on;
- `objectMethod(...)`: what it answers.

When you're done, a metered listener can be unsubscribed with the reference that subscribed it,
a log line that prints it names the listener it meters, and the counts hold only calls made
through the `FareListener` interface. About six lines in total.
