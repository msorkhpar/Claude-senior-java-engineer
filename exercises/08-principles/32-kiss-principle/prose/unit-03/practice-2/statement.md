The page's advice for any shared collection: **return an immutable snapshot**. A snapshot is
a consistent view of one moment, callers can iterate it while other threads keep logging,
and nobody can change the shared state through it.

```java
public List<String> getEvents() {
    return Collections.unmodifiableList(new ArrayList<>(events));
}
```

Write `EventLog`. The page keeps its events in a `CopyOnWriteArrayList`, so that many threads
can log at once; do the same (the tests cannot race threads, so they check the snapshot
rules below, not the list you choose):

- `log(String event)` records an event, every time: an event logged twice appears twice;
- `events()` returns the events so far, oldest first, as a snapshot that is both:
  - **unmodifiable**: `add`, `remove` or `set` on it throws `UnsupportedOperationException`;
  - **a copy**: events logged, or a `clear()`, after the snapshot was taken do not show in it;
- `clear()` forgets every event.

Example: after `log("first")`, a snapshot holds `["first"]`, and it still does after
`log("second")`.
