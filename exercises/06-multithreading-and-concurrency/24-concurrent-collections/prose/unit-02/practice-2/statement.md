The login service keeps the time each session was last seen in a
`Collections.synchronizedMap`. Every request calls `touch`, so the map changes many times a
second. Once a minute a sweeper removes the sessions that have expired, and on busy days it dies
with `ConcurrentModificationException`, leaving stale sessions behind. The debug endpoint that
dumps the sessions has a problem of its own: the dump it is writing out changes while it writes.

Finish the two `TODO`s in `src/main/java/practice/SessionSweeper.java`:

- `sweep(policy)`: remove every session the policy says has expired, and return their ids;
- `snapshot()`: the sessions as they are now, in a map that later requests don't change.

When you're done, a sweep finishes however many requests arrive while it runs, a request that
arrives during a sweep is kept, and a dump doesn't change once it is taken. About ten lines in
total.

The tests send a request into the middle of a sweep, through the expiry policy, and check from
the JVM's thread data whether the request had to wait for the sweep.
