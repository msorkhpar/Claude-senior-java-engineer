The trading desk's monitoring page asks every market-data feed for its `status()` once a second.
When a feed loses its connection, its own thread calls `reconnect()`, which backs off between
attempts, for up to two minutes when the exchange is down. That is exactly when the desk most
wants to see the feed's status, and it is when the monitoring threads stop getting answers. At
shutdown, `close()` should make a reconnect give up, and today shutdown waits out the back-off.

`QuoteFeed` keeps its state under its own monitor, and `reconnect()`, `status()` and `close()`
stay `synchronized`. Finish the two `TODO`s in `src/main/java/practice/QuoteFeed.java`:

- `pause(long millis)`: the back-off between two attempts, called from inside `reconnect()`;
- `close()`: closes the feed, and a reconnect that is backing off gives up at once.

When you're done, `status()` answers during a back-off of any length, and `close()` ends a
back-off in progress. About seven lines in total.

The tests run `reconnect()` on a thread of its own, with back-offs of a few milliseconds and of
two minutes.
